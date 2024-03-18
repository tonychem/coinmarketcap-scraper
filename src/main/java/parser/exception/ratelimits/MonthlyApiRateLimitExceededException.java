package parser.exception.ratelimits;

public class MonthlyApiRateLimitExceededException extends ApiRateLimitExceededException {

    public MonthlyApiRateLimitExceededException(String message) {
        super(message);
    }
}
