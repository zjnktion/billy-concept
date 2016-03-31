package cn.zjnktion.billy.service.server.exception;

/**
 * Created by zhengjn on 2016/3/31.
 */
public class UnbindException extends RuntimeException {

    public UnbindException() {
        super();
    }

    public UnbindException(String message) {
        super(message);
    }

    public UnbindException(Throwable cause) {
        super(cause);
    }

    public UnbindException(String message, Throwable cause) {
        super(message, cause);
    }

}
