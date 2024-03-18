package parser.exception;

/**
 * Исключение, возникающее в отсутствии списка API-токенов
 */
public class CredentialListAbsentException extends RuntimeException {
    public CredentialListAbsentException(String message) {
        super(message);
    }
}
