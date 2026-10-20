package pl.kacper.product_catalog_api.exception;

public class NoSuchDbRecordException extends RuntimeException{

    public NoSuchDbRecordException(String message) {
        super(message);
    }

    public NoSuchDbRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}

