package exception;

public class MonthlyApiRateLimitExceededException extends ApiRateLimitExceededException {
    public MonthlyApiRateLimitExceededException() {
    }

    public MonthlyApiRateLimitExceededException(String message) {
        super(message);
    }
}
