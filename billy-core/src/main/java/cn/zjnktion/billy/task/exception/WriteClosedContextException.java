package cn.zjnktion.billy.task.exception;

import cn.zjnktion.billy.task.WriteTask;

import java.util.Collection;

/**
 * Created by zjnktion on 2016/4/3.
 */
public class WriteClosedContextException extends WriteException {

    public WriteClosedContextException(Collection<WriteTask> tasks, String message, Throwable cause) {
        super(tasks, message, cause);
    }

    public WriteClosedContextException(Collection<WriteTask> tasks, String message) {
        super(tasks, message);
    }

    public WriteClosedContextException(Collection<WriteTask> tasks, Throwable cause) {
        super(tasks, cause);
    }

    public WriteClosedContextException(Collection<WriteTask> tasks) {
        super(tasks);
    }

    public WriteClosedContextException(WriteTask task, String message, Throwable cause) {
        super(task, message, cause);
    }

    public WriteClosedContextException(WriteTask task, String message) {
        super(task, message);
    }

    public WriteClosedContextException(WriteTask task, Throwable cause) {
        super(task, cause);
    }

    public WriteClosedContextException(WriteTask task) {
        super(task);
    }
}
