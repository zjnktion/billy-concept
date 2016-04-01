package cn.zjnktion.billy.listener;

import cn.zjnktion.billy.future.BillyFuture;

/**
 * Created by zjnktion on 2016/3/31.
 */
public class LockNotifyListener implements FutureListener<BillyFuture> {

    private final Object lock;

    public LockNotifyListener(Object lock) {
        this.lock = lock;
    }

    public void operationComplete(BillyFuture future) {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
