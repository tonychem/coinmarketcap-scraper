package parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import parser.exception.ratelimits.ApiRateLimitExceededException;
import parser.model.CryptocurrencyInfo;
import parser.model.UserUsageStatistics;
import utils.entity.Credential;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Реализация клиента рынка.
 */
public class CoinmarketcapClient {

    private final Credential credential;

    private final GenericCoinmarketcapHttpClient httpClient;

    private final DefaultMarketResponseParser parser;

    private final StatisticsManager statisticsManager;

    private final ScheduledExecutorService timelyThreadPool;

    private static final long DELAY_BEFORE_FIRST_STATISTICS_UPDATE_MILLIS = 1_000 * 60 * 15;

    private static final long STATISTICS_UPDATE_EVENT_PERIOD_MILLIS = 1_000 * 60 * 60;

    private static final int QUERIES_PER_CREDIT = 100;

    protected CoinmarketcapClient(Credential credential, String resourceURL, DefaultMarketResponseParser parser) {
        this.credential = credential;
        this.parser = parser;
        this.statisticsManager = new StatisticsManager(credential);
        httpClient = new GenericCoinmarketcapHttpClient(resourceURL);
        timelyThreadPool = Executors.newScheduledThreadPool(1);
        scheduleStatisticsRefreshEvent();
    }

    /**
     * Метод возвращает список информации о криптовалюте по заданному запросу.
     *
     * @param query динамический запрос
     * @return Список информации о криптовалютах, согласно переданному динамическому запросу.
     */
    public List<CryptocurrencyInfo> getCurrencyInfo(DynamicParameterQuery query) throws ApiRateLimitExceededException {
        try {
            HttpResponse<String> response = httpClient.executeGetRequest(query, credential);
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                List<CryptocurrencyInfo> infosList = parser.parseCurrencyInfoList(response.body());
                int queriedParams = query.getQueriedParams();
                int creditsUsed = queriedParams % QUERIES_PER_CREDIT == 0 ?
                        queriedParams / QUERIES_PER_CREDIT : queriedParams / QUERIES_PER_CREDIT + 1;
                statisticsManager.decrementCreditCount(creditsUsed);
                return infosList;
            } else if (statusCode == 429) {
                parser.handleRateLimitViolation(response.body());
            }

            return Collections.emptyList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public UserUsageStatistics getUserUsageStatistics() {
        return statisticsManager.getUsageStatistics();
    }

    /**
     * Метод по запуску периодической актуализации статистики пользователя.
     */
    private void scheduleStatisticsRefreshEvent() {
        Runnable refreshTask = () -> {
            statisticsManager.refreshStatistics();
        };

        timelyThreadPool.scheduleAtFixedRate(refreshTask, DELAY_BEFORE_FIRST_STATISTICS_UPDATE_MILLIS,
                STATISTICS_UPDATE_EVENT_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }
}
