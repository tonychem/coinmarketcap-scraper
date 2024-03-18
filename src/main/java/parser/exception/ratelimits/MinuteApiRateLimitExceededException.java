package parser.exception.ratelimits;

public class MinuteApiRateLimitExceededException extends ApiRateLimitExceededException {

    public MinuteApiRateLimitExceededException(String message) {
        super(message);
    }
}
