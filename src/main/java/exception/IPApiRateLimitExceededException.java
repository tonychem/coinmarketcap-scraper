package exception;

public class IPApiRateLimitExceededException extends ApiRateLimitExceededException {
    public IPApiRateLimitExceededException() {
    }

    public IPApiRateLimitExceededException(String message) {
        super(message);
    }
}
