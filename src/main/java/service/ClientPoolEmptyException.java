package service;

/**
 * Исключение, которое возникает при попытке получить новый клиент из пустого пула
 */
public class ClientPoolEmptyException extends RuntimeException {
    public ClientPoolEmptyException(String message) {
        super(message);
    }
}
