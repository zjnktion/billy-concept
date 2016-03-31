package cn.zjnktion.billy.listener;

import java.util.concurrent.Future;

/**
 * Created by zjnktion on 2016/3/31.
 */
public class LockNotifyListener implements FutureListener<Future> {

    private final Object lock;

    public LockNotifyListener(Object lock) {
        this.lock = lock;
    }

    public void operationComplete(Future future) {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
