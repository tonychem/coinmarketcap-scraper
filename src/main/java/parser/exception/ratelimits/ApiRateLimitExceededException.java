package parser.exception.ratelimits;

/**
 * Базовый класс исключений, описывающих случаи превышения лимитов API
 */
public class ApiRateLimitExceededException extends RuntimeException {
    public ApiRateLimitExceededException() {
    }

    public ApiRateLimitExceededException(String message) {
        super(message);
    }
}
