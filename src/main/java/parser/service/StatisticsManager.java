package parser.service;

import parser.exception.ratelimits.ApiRateLimitExceededException;
import parser.exception.ratelimits.MinuteApiRateLimitExceededException;
import parser.exception.ratelimits.MonthlyApiRateLimitExceededException;
import parser.model.CreditsInfo;
import parser.model.MinuteUsage;
import parser.model.UserUsageStatistics;
import utils.entity.Credential;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static utils.ApplicationConstantHolder.DEFAULT_ZONE_OFFSET;

/**
 * Класс - менеджер, отслеживающий и кэширующий информацию о состоянии лимитов API для данного клиента.
 */
public class StatisticsManager {
    private static final int API_MINUTE_RATE_LIMIT = 30;
    private static final int API_MONTH_RATE_LIMIT = 10_000;
    private final UserUsageStatisticGatherer userUsageStatisticGatherer;

    private final Credential credential;

    private volatile UserUsageStatistics usageStatistics;

    private OffsetDateTime lastUpdated;

    public StatisticsManager(Credential credential) {
        this.userUsageStatisticGatherer = new UserUsageStatisticGatherer();
        this.credential = credential;
        usageStatistics = userUsageStatisticGatherer.getStatistics(credential);
        lastUpdated = usageStatistics.getCreatedAt();
    }

    /**
     * Управление минутными и месячными счетчиками лимитов.
     * @param credits количество кредитов, которое необходимо списать
     * @throws ApiRateLimitExceededException
     */
    public synchronized void decrementCreditCount(int credits) throws ApiRateLimitExceededException {
        manageMonthlyCreditCounter(credits);
        manageMinuteRequestCounter();
        usageStatistics.setCreatedAt(Instant.now().atOffset(DEFAULT_ZONE_OFFSET));
    }

    /**
     * Метод для управления минутным счетчиком лимитов. Уменьшает количество доступных запросов на 1, если
     * метод уже был вызван в последнюю минуту. Сбрасывает лимиты, если с последнего запроса прошло больше 1 минуты.
     * Бросает исключение, если минутный лимит был превышен.
     * @throws MinuteApiRateLimitExceededException
     */
    private void manageMinuteRequestCounter() throws MinuteApiRateLimitExceededException {
        OffsetDateTime currentMoment = Instant.now()
                .atOffset(DEFAULT_ZONE_OFFSET);

        if (usageStatistics.getCurrentMinuteUsage().getRequestsLeft() == 0) {
            throw new MinuteApiRateLimitExceededException("Exceeded minute api rate limit!");
        }

        if (ChronoUnit.SECONDS.between(lastUpdated, currentMoment) >= 60) {
            usageStatistics.getCurrentMinuteUsage().setRequestsLeft((long) API_MINUTE_RATE_LIMIT);
            usageStatistics.getCurrentMinuteUsage().setRequestsMade(0L);
        }

        MinuteUsage currentMinuteUsage = usageStatistics.getCurrentMinuteUsage();
        Long prevRequestsMadeCount = currentMinuteUsage.getRequestsMade();
        Long prevRequestsLeftCount = currentMinuteUsage.getRequestsLeft();

        currentMinuteUsage.setRequestsMade(++prevRequestsMadeCount);
        currentMinuteUsage.setRequestsLeft(--prevRequestsLeftCount);
    }

    /**
     * Метод для управления месячным счетчиком лимитов. Если между текущим и последним запросом прошло больше 1 месяца,
     * счетчик восстаналивает значения лимитов. Счетчик уменьшает количество доступных кредитов на значение credits, если
     * прошло меньше 1 месяца. Выбрасывает исключение, если отсутствуют досутпные запросы.
     * @param credits количество кредитов для списания
     * @throws ApiRateLimitExceededException
     */
    private void manageMonthlyCreditCounter(int credits) throws ApiRateLimitExceededException {
        OffsetDateTime currentMoment = Instant.now().atOffset(DEFAULT_ZONE_OFFSET);

        if (usageStatistics.getCurrentMonthUsage().getCreditsLeft() == 0) {
            throw new MonthlyApiRateLimitExceededException("Exceeded minute api rate limit!");
        } else if (usageStatistics.getCurrentMonthUsage().getCreditsLeft() - credits < 0) {
            throw new ApiRateLimitExceededException("Not enough credits left to perform such an operation");
        }

        if (ChronoUnit.MONTHS.between(lastUpdated, currentMoment) >= 1) {
            usageStatistics.getCurrentMonthUsage().setCreditsLeft((long) API_MONTH_RATE_LIMIT);
            usageStatistics.getCurrentMonthUsage().setCreditsUsed(0L);
        }

        CreditsInfo monthlyUsage = usageStatistics.getCurrentMonthUsage();
        Long prevCreditsUsedCount = monthlyUsage.getCreditsUsed();
        Long prevCreditsLeftCount = monthlyUsage.getCreditsLeft();

        long newCreditsUsedCount = prevCreditsUsedCount + credits;
        long newCreditsLeftCount = prevCreditsLeftCount - credits;

        monthlyUsage.setCreditsUsed(newCreditsUsedCount);
        monthlyUsage.setCreditsLeft(newCreditsLeftCount);
    }

    /**
     * Метод для получения информации о лимитах с сервера. (актуализация кэш-данных)
     */
    public synchronized void refreshStatistics() {
        usageStatistics = userUsageStatisticGatherer.getStatistics(credential);
        lastUpdated = usageStatistics.getCreatedAt();
    }

    public synchronized UserUsageStatistics getUsageStatistics() {
        return usageStatistics;
    }
}
