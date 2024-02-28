package httpclient;

import exception.ApiRateLimitExceededException;
import exception.MinuteApiRateLimitExceededException;
import exception.MonthlyApiRateLimitExceededException;
import model.Credential;
import model.CreditsInfo;
import model.MinuteUsage;
import model.UserUsageStatistics;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class StatisticsManager {
    private static final int API_MINUTE_RATE_LIMIT = 30;
    private static final int API_MONTH_RATE_LIMIT = 10_000;
    private final UserUsageStatisticGatherer userUsageStatisticGatherer;

    private final Credential credential;

    private volatile UserUsageStatistics usageStatistics;

    private LocalDateTime lastUpdated;

    public StatisticsManager(Credential credential) {
        this.userUsageStatisticGatherer = new UserUsageStatisticGatherer();
        this.credential = credential;
        usageStatistics = userUsageStatisticGatherer.getStatistics(credential);
        lastUpdated = usageStatistics.getCreatedAt();
    }

    public synchronized void decrementCreditCount(int credits) throws ApiRateLimitExceededException {
        manageMonthlyCreditCounter(credits);
        manageMinuteRequestCounter();
    }

    private void manageMinuteRequestCounter() throws MinuteApiRateLimitExceededException {
        LocalDateTime currentMoment = LocalDateTime.now();

        if (usageStatistics.getCurrentMinuteUsage().getRequestsLeft() == 0) {
            throw new MinuteApiRateLimitExceededException("Exceeded minute api rate limit!");
        }

        if (ChronoUnit.SECONDS.between(lastUpdated, currentMoment) >= 60) {
            usageStatistics.getCurrentMinuteUsage().setRequestsLeft((long) API_MINUTE_RATE_LIMIT);
        }

        MinuteUsage currentMinuteUsage = usageStatistics.getCurrentMinuteUsage();
        Long prevRequestsMadeCount = currentMinuteUsage.getRequestsMade();
        Long prevRequestsLeftCount = currentMinuteUsage.getRequestsLeft();

        currentMinuteUsage.setRequestsMade(++prevRequestsMadeCount);
        currentMinuteUsage.setRequestsLeft(--prevRequestsLeftCount);

        lastUpdated = currentMoment;
    }

    private void manageMonthlyCreditCounter(int credits) throws ApiRateLimitExceededException {
        LocalDateTime currentMoment = LocalDateTime.now();

        if (usageStatistics.getCurrentMonthUsage().getCreditsLeft() == 0) {
            throw new MonthlyApiRateLimitExceededException("Exceeded minute api rate limit!");
        } else if (usageStatistics.getCurrentMonthUsage().getCreditsLeft() - credits < 0) {
            throw new ApiRateLimitExceededException("Not enough credits left to perform such an operation");
        }

        if (ChronoUnit.MONTHS.between(lastUpdated, currentMoment) >= 1) {
            usageStatistics.getCurrentMonthUsage().setCreditsLeft((long) API_MONTH_RATE_LIMIT);
        }

        CreditsInfo monthlyUsage = usageStatistics.getCurrentMonthUsage();
        Long prevCreditsUsedCount = monthlyUsage.getCreditsUsed();
        Long prevCreditsLeftCount = monthlyUsage.getCreditsLeft();

        long newCreditsUsedCount = prevCreditsUsedCount + credits;
        long newCreditsLeftCount = prevCreditsLeftCount - credits;

        monthlyUsage.setCreditsUsed(newCreditsUsedCount);
        monthlyUsage.setCreditsLeft(newCreditsLeftCount);

        lastUpdated = currentMoment;
    }

    public synchronized void refreshStatistics() {
        usageStatistics = userUsageStatisticGatherer.getStatistics(credential);
        lastUpdated = usageStatistics.getCreatedAt();
    }
}
