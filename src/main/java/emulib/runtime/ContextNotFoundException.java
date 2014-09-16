package emulib.runtime;

public class ContextNotFoundException extends Exception {

    public ContextNotFoundException() {
    }

    public ContextNotFoundException(String message) {
        super(message);
    }

    public ContextNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContextNotFoundException(Throwable cause) {
        super(cause);
    }

    public ContextNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    

}
