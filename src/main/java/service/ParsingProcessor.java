package service;

import client.ClientTask;
import client.CoinmarketcapClient;
import client.CoinmarketcapClientPool;
import client.DynamicParameterQuery;
import model.CryptocurrencyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Центральный класс для управления загрузкой данных о криптовалютах из нескольких клиентов. Класс содержит пул
 * активных клиентов и методы для работы с ними.
 */
public class ParsingProcessor {
    private final CoinmarketcapClientPool clientPool;

    private final ExecutorService executorService;

    private final Timer timer = new Timer(true);

    private final CopyOnWriteArrayList<CryptocurrencyInfo> resultList;

    private boolean hasStarted = false;

    /**
     * Переменная обозначает, какое максимальное количество символов криптовалют может выполнять один запрос клиента.
     */
    private static final int SYMBOLS_PER_CLIENT = 100;

    /**
     * Таймер с периодичностью TASK_REFRESH_PERIOD выполняет запуск методов загрузки данных (мс).
     */

    private static final int TASK_REFRESH_PERIOD = 1_000 * 60;


    public ParsingProcessor(CoinmarketcapClientPool clientPool) {
        this.clientPool = clientPool;

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
        resultList = new CopyOnWriteArrayList<>();
    }

    /**
     * Публичный метод, запускающий процесс сбора информации. Декорирует запуск таймера с набором инструкций
     * executeParsing
     * @param symbols список символов криптовалют
     */
    public void start(String... symbols) {
        if (hasStarted) {
            throw new IllegalStateException("Already started");
        }

        if (symbols == null || symbols.length == 0) {
            throw new IllegalStateException("Symbol list is empty or null");
        }

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("MAIN-PARSER-THREAD");
                executeParsing(symbols);
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, TASK_REFRESH_PERIOD);
        hasStarted = true;
    }

    /**
     * Набор инструкций по запуску сбора информации. Перед каждым запуском происходит очистка списка загруженных данных и
     * проверка клиентов, чей месячный api лимит закончился.
     * @param symbols список символов криптовалют
     */
    private void executeParsing(String... symbols) {
        try {
            resultList.clear();
            clientPool.removeExpiredClients();

            List<Future<List<CryptocurrencyInfo>>> futureList = initiateWorkers(symbols);
            List<CryptocurrencyInfo> infos = collectParsedInfo(futureList);

            resultList.addAll(infos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод раздает задания клиентам из пула. Для этого формируется массив динамических параметров запроса (по
     * SYMBOLS_PER_CLIENT в каждом) и рассматривается две ситуации: когда заданий больше, чем клиентов, и обратное. В первом случае
     * каждому клиенту дается еще 1 дополнительное задание. Для поддержания честности по отношению к клиентам, каждый клиент находится
     * в PriorityQueue, компарированной по количеству оставшихся месячных запросов. После отправки задания на клиент и списания кредитов,
     * клиент встает обратно в очередь.
     * @param symbols список символов криптовалют
     * @return
     */
    private List<Future<List<CryptocurrencyInfo>>> initiateWorkers(String... symbols) {
        DynamicParameterQuery[] queries = queryBySymbols(symbols);
        List<Future<List<CryptocurrencyInfo>>> futureResultList = new ArrayList<>();

        if (queries.length <= clientPool.getPoolSize()) {
            for (DynamicParameterQuery currentQuery : queries) {
                CoinmarketcapClient currentClient = clientPool.getClient();

                Future<List<CryptocurrencyInfo>> futureCryptocurrencyList = executorService.submit(
                        new ClientTask(currentClient, currentQuery)
                );

                clientPool.addClient(currentClient);
                futureResultList.add(futureCryptocurrencyList);
            }
        } else {
            int minTaskAmount = symbols.length / clientPool.getPoolSize();
            int plusOneTaskWorkers = symbols.length % clientPool.getPoolSize();
            int clientCount = 0;

            CoinmarketcapClient client = clientPool.getClient();
            int queryArrayIndex = 0;

            while (queryArrayIndex < queries.length) {
                int queriesToAdd = clientCount < plusOneTaskWorkers - 1 ? minTaskAmount + 1 : minTaskAmount;

                for (int i = 0; i < queriesToAdd; i++) {
                    Future<List<CryptocurrencyInfo>> futureCryptocurrencyList = executorService.submit(
                            new ClientTask(client, queries[queryArrayIndex++])
                    );
                    futureResultList.add(futureCryptocurrencyList);
                }

                clientPool.addClient(client);
                client = clientPool.getClient();
                clientCount++;
            }
        }

        return futureResultList;
    }

    private List<CryptocurrencyInfo> collectParsedInfo(List<Future<List<CryptocurrencyInfo>>> futureList)
            throws ExecutionException, InterruptedException, TimeoutException {
        List<CryptocurrencyInfo> infos = new ArrayList<>();

        for (Future<List<CryptocurrencyInfo>> singleFutureResultList : futureList) {
            infos.addAll(singleFutureResultList.get(30, TimeUnit.SECONDS));
        }
        return infos;
    }

    public List<CryptocurrencyInfo> getLatestResultSet() {
        return new ArrayList<>(resultList);
    }

    /**
     * Формирует динамические параметры запросов по заданному массиву символов криптовалют. В каждый запрос попадает не более
     * SYMBOLS_PER_CLIENT символов.
     * @param symbols список символов криптовалют
     */
    public DynamicParameterQuery[] queryBySymbols(String... symbols) {
        int dynamicQueryArraySize = symbols.length % SYMBOLS_PER_CLIENT == 0 ?
                symbols.length / SYMBOLS_PER_CLIENT : symbols.length / SYMBOLS_PER_CLIENT + 1;

        DynamicParameterQuery.QueryBuilder[] queryBuilderArray
                = new DynamicParameterQuery.QueryBuilder[dynamicQueryArraySize];
        DynamicParameterQuery[] queryArray = new DynamicParameterQuery[queryBuilderArray.length];

        for (int i = 0; i < queryBuilderArray.length; i++) {
            queryBuilderArray[i] = new DynamicParameterQuery.QueryBuilder();

            for (int j = SYMBOLS_PER_CLIENT * i; j < symbols.length && j < SYMBOLS_PER_CLIENT * (i + 1); j++) {
                queryBuilderArray[i].symbol(symbols[j]);
            }

            queryArray[i] = queryBuilderArray[i].build();
        }

        return queryArray;
    }
}
