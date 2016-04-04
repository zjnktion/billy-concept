package cn.zjnktion.billy.task;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.future.WriteFuture;
import cn.zjnktion.billy.listener.FutureListener;

import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by zjnktion on 2016/4/3.
 */
public class DefaultWriteTask implements WriteTask {

    /**
     * 空消息常量
     */
    public static final byte[] EMPTY_MESSAGE = new byte[] {};

    private final Object message;

    private final WriteFuture future;

    private final SocketAddress destination;

    public DefaultWriteTask(Object message) {
        this(message, null, null);
    }

    public DefaultWriteTask(Object message, WriteFuture future) {
        this(message, future, null);
    }

    public DefaultWriteTask(Object message, WriteFuture future, SocketAddress destination) {
        if (message == null) {
            throw new IllegalArgumentException("Cannot set a null message to write task.");
        }

        if (future == null) {
            future = NONE_FUTURE;
        }

        this.message = message;
        this.future = future;
        this.destination = destination;
    }

    public WriteTask getOriginalTask() {
        return this;
    }

    public boolean isEncoded() {
        return false;
    }

    public Object getMessage() {
        return message;
    }

    public WriteFuture getFuture() {
        return future;
    }

    public SocketAddress getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("WriteTask: ");

        if (message.getClass().getName().equals(Object.class.getName())) {
            sb.append("CLOSE_TASK");
        }
        else {
            if (getDestination() == null) {
                sb.append(message);
            }
            else {
                sb.append(message);
                sb.append(" => ");
                sb.append(getDestination());
            }
        }

        return sb.toString();
    }

    private static final WriteFuture NONE_FUTURE = new WriteFuture() {
        public WriteFuture await() throws InterruptedException {
            return this;
        }

        public WriteFuture awaitUninterruptibly() {
            return this;
        }

        public WriteFuture addListener(FutureListener<?> listener) {
            throw new UnsupportedOperationException("Cannot set a listener to none write future.");
        }

        public WriteFuture removeListener(FutureListener<?> listener) {
            throw new UnsupportedOperationException("Cannot del a listener from none write future.");
        }

        public boolean isWritten() {
            return true;
        }

        public void setWritten() {

        }

        public Throwable getCause() {
            return null;
        }

        public void setCause(Throwable cause) {

        }

        public Context getContext() {
            return null;
        }

        public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
            return true;
        }

        public boolean awaitUninterruptibly(long timeout, TimeUnit timeUnit) {
            return true;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }

        public boolean isCancelled() {
            return true;
        }

        public boolean isDone() {
            return true;
        }

        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    };

}
