package cn.zjnktion.billy.future.exception;

/**
 * Created by zjnktion on 2016/4/2.
 */
public class FutureException extends RuntimeException {

    public FutureException() {
        super();
    }

    public FutureException(String message) {
        super(message);
    }

    public FutureException(Throwable cause) {
        super(cause);
    }

    public FutureException(String message, Throwable cause) {
        super(message, cause);
    }

}
