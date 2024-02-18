package exception;

public class MinuteApiRateLimitExceededException extends ApiRateLimitExceededException {
    public MinuteApiRateLimitExceededException() {
    }

    public MinuteApiRateLimitExceededException(String message) {
        super(message);
    }
}
