package parser.exception.ratelimits;

public class ApiRateLimitExceededException extends RuntimeException {
    public ApiRateLimitExceededException() {
    }

    public ApiRateLimitExceededException(String message) {
        super(message);
    }
}
