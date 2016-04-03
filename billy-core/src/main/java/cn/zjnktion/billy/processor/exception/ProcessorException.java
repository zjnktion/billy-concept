package cn.zjnktion.billy.processor.exception;

/**
 * Created by zjnktion on 2016/4/3.
 */
public class ProcessorException extends RuntimeException {

    public ProcessorException() {
        super();
    }

    public ProcessorException(String message) {
        super(message);
    }

    public ProcessorException(Throwable cause) {
        super(cause);
    }

    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
    }

}
