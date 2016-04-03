package cn.zjnktion.billy.future;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.listener.FutureListener;

/**
 * Created by zjnktion on 2016/4/3.
 */
public class DefaultWriteFuture extends FutureTemplate implements WriteFuture {

    public DefaultWriteFuture(Context context) {
        super(context);
    }

    public WriteFuture await() throws InterruptedException {
        return (WriteFuture) super.await();
    }

    public WriteFuture awaitUninterruptibly() {
        return (WriteFuture) super.awaitUninterruptibly();
    }

    public WriteFuture addListener(FutureListener<?> listener) {
        return (WriteFuture) super.addListener(listener);
    }

    public WriteFuture removeListener(FutureListener<?> listener) {
        return (WriteFuture) super.removeListener(listener);
    }

    public boolean isWritten() {
        if (isDone()) {
            Object o = getResult();

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }
        }

        return false;
    }

    public void setWritten() {
        setResult(Boolean.TRUE);
    }

    public Throwable getCause() {
        if (isDone()) {
            Object o = getResult();

            if (o instanceof  Throwable) {
                return (Throwable) o;
            }
        }

        return null;
    }

    public void setCause(Throwable cause) {
        if (cause == null) {
            throw new IllegalArgumentException("Cannot set a null cause to write future.");
        }

        setResult(cause);
    }

    @Override
    protected final boolean needCheckDeadlock() {
        return true;
    }

    public static WriteFuture newWrittenFuture(Context context) {
        DefaultWriteFuture writtenFuture = new DefaultWriteFuture(context);
        writtenFuture.setWritten();
        return writtenFuture;
    }

    public static WriteFuture newCauseWriteFuture(Context context, Throwable cause) {
        DefaultWriteFuture causeWriteFuture = new DefaultWriteFuture(context);
        causeWriteFuture.setCause(cause);
        return causeWriteFuture;
    }

}
