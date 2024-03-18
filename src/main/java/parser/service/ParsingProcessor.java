package parser.service;

import parser.model.CryptocurrencyInfo;
import parser.repository.RepositoryManager;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static utils.ApplicationConstantHolder.INDICES_NAME_FORMAT;

/**
 * Центральный класс для управления загрузкой данных о криптовалютах из нескольких клиентов. Класс содержит пул
 * активных клиентов и методы для работы с ними.
 */
public class ParsingProcessor {
    private final CoinmarketcapClientPool clientPool;

    private final RepositoryManager repositoryManager;

    private final ExecutorService workerThreadPool;
    private final ScheduledExecutorService repeatedTasksThreadPool;

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


    public ParsingProcessor(CoinmarketcapClientPool clientPool, RepositoryManager repositoryManager) {
        this.clientPool = clientPool;
        this.repositoryManager = repositoryManager;
        workerThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
        resultList = new CopyOnWriteArrayList<>();
        repeatedTasksThreadPool = Executors.newScheduledThreadPool(2);
    }

    /**
     * Публичный метод, запускающий процесс сбора информации. Декорирует запуск таймера с набором инструкций
     * executeParsing и flushLatestResult.
     *
     * @param symbols список символов криптовалют
     */
    public void start(String... symbols) {
        if (hasStarted) {
            throw new IllegalStateException("Сервис уже запущен!");
        }

        if (symbols == null || symbols.length == 0) {
            throw new IllegalStateException("Список тикеров для отслеживания пуст или null.");
        }

        Runnable parsingTask = () -> {
            executeParsing(symbols);
        };

        Runnable saverTask = () -> {
            flushLatestResult();
        };

        repeatedTasksThreadPool.scheduleAtFixedRate(parsingTask, 0, TASK_REFRESH_PERIOD, TimeUnit.MILLISECONDS);
        repeatedTasksThreadPool.scheduleAtFixedRate(saverTask, TASK_REFRESH_PERIOD / 2,
                TASK_REFRESH_PERIOD, TimeUnit.MILLISECONDS);

        hasStarted = true;
    }

    /**
     * Набор инструкций по запуску сбора информации. Перед каждым запуском происходит очистка списка загруженных данных и
     * проверка клиентов, чей месячный api лимит закончился.
     *
     * @param symbols список символов криптовалют
     */
    public void executeParsing(String... symbols) {
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
     * Набор инструкций для сохранения последних сохраненных записей о курсах криптовалют, собранных клиентами.
     */
    private void flushLatestResult() {
        try {
            for (CryptocurrencyInfo info : resultList) {
                OffsetDateTime infoLastUpdatedAt = info.getLastUpdated();
                String currentIndex = String.format(INDICES_NAME_FORMAT, info.getTicker().toLowerCase(), infoLastUpdatedAt.getYear(),
                        infoLastUpdatedAt.getMonthValue(), infoLastUpdatedAt.getDayOfMonth());

                if (!repositoryManager.checkIndexExist(currentIndex)) {
                    repositoryManager.createIndex(currentIndex);
                }

                repositoryManager.saveCryptocurrencyInfo(currentIndex, info);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод раздает задания клиентам из пула. Для этого формируется массив динамических параметров запроса (по
     * SYMBOLS_PER_CLIENT в каждом) и рассматривается две ситуации: когда заданий больше, чем клиентов, и обратное. В первом случае
     * каждому клиенту дается еще 1 дополнительное задание. Для поддержания честности по отношению к клиентам, каждый клиент находится
     * в PriorityQueue, компарированной по количеству оставшихся месячных запросов. После отправки задания на клиент и списания кредитов,
     * клиент встает обратно в очередь.
     *
     * @param symbols список символов криптовалют
     * @return
     */
    private List<Future<List<CryptocurrencyInfo>>> initiateWorkers(String... symbols) {
        DynamicParameterQuery[] queries = queryBySymbols(symbols);
        List<Future<List<CryptocurrencyInfo>>> futureResultList = new ArrayList<>();

        if (queries.length <= clientPool.getPoolSize()) {
            for (DynamicParameterQuery currentQuery : queries) {
                CoinmarketcapClient currentClient = clientPool.getClient();

                Future<List<CryptocurrencyInfo>> futureCryptocurrencyList = workerThreadPool.submit(
                        new ClientTask(currentClient, currentQuery)
                );

                clientPool.addClient(currentClient);
                futureResultList.add(futureCryptocurrencyList);
            }
        } else {
            int minTaskAmount = queries.length / clientPool.getPoolSize();
            int tasksLeftToDelegate = queries.length % clientPool.getPoolSize();

            //добавляем минимальное количество заданий на все клиенты
            int queryArrayIndex = 0;
            int clientPoolSize = clientPool.getPoolSize();
            while (clientPoolSize > 0) {
                CoinmarketcapClient client = clientPool.getClient();
                for (int i = 0; i < minTaskAmount; i++) {
                    Future<List<CryptocurrencyInfo>> futureCryptocurrencyList = workerThreadPool.submit(
                            new ClientTask(client, queries[queryArrayIndex++])
                    );
                    futureResultList.add(futureCryptocurrencyList);
                }

                clientPool.addClient(client);
                clientPoolSize--;
            }

            //добавляем остаток, по одному на каждый клиент
            while (tasksLeftToDelegate > 0) {
                CoinmarketcapClient client = clientPool.getClient();
                Future<List<CryptocurrencyInfo>> futureCryptocurrencyList = workerThreadPool.submit(
                        new ClientTask(client, queries[queryArrayIndex++])
                );
                futureResultList.add(futureCryptocurrencyList);
                clientPool.addClient(client);
                tasksLeftToDelegate--;
            }
        }

        return futureResultList;
    }

    /**
     * Метод распаковывает фьючерсы информации о криптовалютах в соответствующий список
     * @param futureList список фьючерсов
     * @return список информации о криптовалютах
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private List<CryptocurrencyInfo> collectParsedInfo(List<Future<List<CryptocurrencyInfo>>> futureList)
            throws ExecutionException, InterruptedException, TimeoutException {
        List<CryptocurrencyInfo> infos = new ArrayList<>();

        for (Future<List<CryptocurrencyInfo>> singleFutureResultList : futureList) {
            infos.addAll(singleFutureResultList.get(TASK_REFRESH_PERIOD / 4, TimeUnit.MILLISECONDS));
        }
        return infos;
    }

    /**
     * Формирует динамические параметры запросов по заданному массиву символов криптовалют. В каждый запрос попадает не более
     * SYMBOLS_PER_CLIENT символов.
     *
     * @param symbols список символов криптовалют
     */
    private DynamicParameterQuery[] queryBySymbols(String... symbols) {
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
