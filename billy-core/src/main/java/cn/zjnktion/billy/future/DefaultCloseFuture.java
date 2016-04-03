package cn.zjnktion.billy.future;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.listener.FutureListener;

/**
 * Created by zjnktion on 2016/4/3.
 */
public class DefaultCloseFuture extends FutureTemplate implements CloseFuture {

    public DefaultCloseFuture(Context context) {
        super(context);
    }

    public CloseFuture await() throws InterruptedException {
        return (CloseFuture) super.await();
    }

    public CloseFuture awaitUninterruptibly() {
        return (CloseFuture) super.awaitUninterruptibly();
    }

    public CloseFuture addListener(FutureListener<?> listener) {
        return (CloseFuture) super.addListener(listener);
    }

    public CloseFuture removeListener(FutureListener<?> listener) {
        return (CloseFuture) super.removeListener(listener);
    }

    public boolean isClosed() {
        if (isDone()) {
            Object o = getResult();
            return ((Boolean) o).booleanValue();
        }

        return false;
    }

    public void setClosed() {
        setResult(Boolean.TRUE);
    }

    @Override
    protected final boolean needCheckDeadlock() {
        return true;
    }

}
