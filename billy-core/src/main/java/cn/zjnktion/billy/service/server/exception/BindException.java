package cn.zjnktion.billy.service.server.exception;

/**
 * 自定义端口绑定异常
 * Created by zhengjn on 2016/3/31.
 */
public class BindException extends RuntimeException {

    public BindException() {
        super();
    }

    public BindException(String message) {
        super(message);
    }

    public BindException(Throwable cause) {
        super(cause);
    }

    public BindException(String message, Throwable cause) {
        super(message, cause);
    }

}
