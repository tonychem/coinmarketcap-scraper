package client.exception;

public class DailyApiRateLimitExceededException extends ApiRateLimitExceededException {
    public DailyApiRateLimitExceededException() {
    }

    public DailyApiRateLimitExceededException(String message) {
        super(message);
    }
}
