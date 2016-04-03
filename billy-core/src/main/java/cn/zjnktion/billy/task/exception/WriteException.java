package cn.zjnktion.billy.task.exception;

import cn.zjnktion.billy.common.MapBackedSet;
import cn.zjnktion.billy.task.WriteTask;

import java.io.IOException;
import java.util.*;

/**
 * Created by zjnktion on 2016/4/3.
 */
public class WriteException extends IOException {

    private final List<WriteTask> tasks;

    public WriteException(WriteTask task) {
        super();
        this.tasks = astaskList(task);
    }

    public WriteException(WriteTask task, String message) {
        super(message);
        this.tasks = astaskList(task);
    }

    public WriteException(WriteTask task, String message, Throwable cause) {
        super(message);
        initCause(cause);
        this.tasks = astaskList(task);
    }

    public WriteException(WriteTask task, Throwable cause) {
        initCause(cause);
        this.tasks = astaskList(task);
    }

    public WriteException(Collection<WriteTask> tasks) {
        super();
        this.tasks = astaskList(tasks);
    }

    public WriteException(Collection<WriteTask> tasks, String message) {
        super(message);
        this.tasks = astaskList(tasks);
    }

    public WriteException(Collection<WriteTask> tasks, String message, Throwable cause) {
        super(message);
        initCause(cause);
        this.tasks = astaskList(tasks);
    }

    public WriteException(Collection<WriteTask> tasks, Throwable cause) {
        initCause(cause);
        this.tasks = astaskList(tasks);
    }

    public List<WriteTask> gettasks() {
        return tasks;
    }

    public WriteTask gettask() {
        return tasks.get(0);
    }

    private static List<WriteTask> astaskList(Collection<WriteTask> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("tasks");
        }

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("tasks is empty.");
        }

        Set<WriteTask> newtasks = new MapBackedSet<WriteTask>(new LinkedHashMap<WriteTask, Boolean>());

        for (WriteTask t : tasks) {
            newtasks.add(t.getOriginalTask());
        }

        return Collections.unmodifiableList(new ArrayList<WriteTask>(newtasks));
    }

    private static List<WriteTask> astaskList(WriteTask task) {
        if (task == null) {
            throw new IllegalArgumentException("Cannot deal a null task.");
        }

        List<WriteTask> tasks = new ArrayList<WriteTask>(1);
        tasks.add(task.getOriginalTask());

        return Collections.unmodifiableList(tasks);
    }

}
