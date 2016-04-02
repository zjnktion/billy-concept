package cn.zjnktion.billy.future;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.future.exception.FutureException;
import cn.zjnktion.billy.listener.FutureListener;

import java.io.IOException;

/**
 * Created by zjnktion on 2016/4/2.
 */
public class DefaultReadFuture extends FutureTemplate implements ReadFuture {

    private static final Object CLOSED = new Object();

    public DefaultReadFuture(Context context) {
        super(context);
    }

    public ReadFuture await() throws InterruptedException {
        return (ReadFuture) super.await();
    }

    public ReadFuture awaitUninterruptibly() {
        return (ReadFuture) super.awaitUninterruptibly();
    }

    public ReadFuture addListener(FutureListener<?> listener) {
        return (ReadFuture) super.addListener(listener);
    }

    public ReadFuture removeListener(FutureListener<?> listener) {
        return (ReadFuture) super.removeListener(listener);
    }

    public Object getMessage() {
        if (isDone()) {
            Object v = getResult();

            if (v == CLOSED) {
                return null;
            }

            if (v instanceof RuntimeException) {
                throw (RuntimeException) v;
            }

            if (v instanceof Error) {
                throw (Error) v;
            }

            if (v instanceof IOException || v instanceof Exception) {
                throw new FutureException((Exception) v);
            }

            return v;
        }

        return null;
    }

    public boolean isRead() {
        if (isDone()) {
            Object v = getResult();

            return (v != CLOSED && !(v instanceof Throwable));
        }

        return false;
    }

    public void setRead(Object message) {
        if (message == null) {
            throw new IllegalArgumentException("Cannot set a null message.");
        }

        setResult(message);
    }

    public boolean isClosed() {
        return isDone() && getResult() == CLOSED;
    }

    public void setClosed() {
        setResult(CLOSED);
    }

    public Throwable getCause() {
        if (isDone()) {
            Object v = getResult();

            if (v instanceof Throwable) {
                return (Throwable)v;
            }
        }

        return null;
    }

    public void setCause(Throwable cause) {
        if (cause == null) {
            throw new IllegalArgumentException("exception");
        }

        setResult(cause);
    }

    @Override
    protected boolean needCheckDeadlock() {
        return true;
    }
}
