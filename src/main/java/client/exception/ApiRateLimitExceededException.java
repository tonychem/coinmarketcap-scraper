package client.exception;

public class ApiRateLimitExceededException extends RuntimeException {
    public ApiRateLimitExceededException() {
    }

    public ApiRateLimitExceededException(String message) {
        super(message);
    }
}
