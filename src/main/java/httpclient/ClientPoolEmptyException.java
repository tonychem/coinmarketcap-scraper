package httpclient;

public class ClientPoolEmptyException extends RuntimeException {
    public ClientPoolEmptyException(String message) {
        super(message);
    }
}
