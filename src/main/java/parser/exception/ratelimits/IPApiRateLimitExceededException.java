package parser.exception.ratelimits;

public class IPApiRateLimitExceededException extends ApiRateLimitExceededException {
    public IPApiRateLimitExceededException() {
    }

    public IPApiRateLimitExceededException(String message) {
        super(message);
    }
}
