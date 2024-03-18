package parser.exception.ratelimits;

public class IPApiRateLimitExceededException extends ApiRateLimitExceededException {
    public IPApiRateLimitExceededException(String message) {
        super(message);
    }
}
