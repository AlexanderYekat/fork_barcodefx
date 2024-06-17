package site.barsukov.barcodefx.exception;

public class AppException extends RuntimeException{

    public AppException(Exception e) {
        super(e);
    }

    public AppException(String message) {
        super(message);
    }
}
