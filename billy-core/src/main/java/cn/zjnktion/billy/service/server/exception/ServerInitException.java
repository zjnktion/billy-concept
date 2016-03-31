package cn.zjnktion.billy.service.server.exception;

/**
 * Created by zhengjn on 2016/3/31.
 */
public class ServerInitException extends RuntimeException {

    public ServerInitException() {
        super();
    }

    public ServerInitException(String message) {
        super(message);
    }

    public ServerInitException(Throwable cause) {
        super(cause);
    }

    public ServerInitException(String message, Throwable cause) {
        super(message, cause);
    }

}
