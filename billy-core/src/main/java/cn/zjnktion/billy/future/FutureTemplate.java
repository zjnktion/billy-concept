package cn.zjnktion.billy.future;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.listener.FutureListener;
import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.service.server.NioServerTemplete;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhengjn on 2016/4/1.
 */
public abstract class FutureTemplate implements BillyFuture {

    private final Context context;

    private List<FutureListener<?>> listeners = new ArrayList<FutureListener<?>>();

    private Object result;

    private boolean completed;

    private static final long DEADLOCK_CHECK_INTERVAL = 5000L;
    private final Object lock;
    private int waitCount = 0;

    public FutureTemplate(Context context) {
        this.context = context;
        lock = this;
    }

    public Context getContext() {
        return context;
    }

    public BillyFuture addListener(FutureListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot set a null listener.");
        }

        synchronized (lock) {
            if (completed) {
                // 如果当前future已经完成了，我们往当前future加listener其实也就是让该listener直接执行就行了
                // 因为其他listener在之前已经被执行了
                notifyListener(listener);
            } else {
                listeners.add(listener);
            }
        }

        return this;
    }

    public BillyFuture removeListener(FutureListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot remove a null listener.");
        }

        synchronized (lock) {
            if (!completed) {
                listeners.remove(listener);
            }
        }

        return this;
    }

    public BillyFuture await() throws InterruptedException {
        synchronized (lock) {
            while (!completed) {
                waitCount++;
                try {
                    lock.wait(DEADLOCK_CHECK_INTERVAL);
                } finally {
                    waitCount--;

                    if (!completed) {
                        checkDeadlock();
                    }
                }
            }
        }

        return this;
    }

    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return await_(timeUnit.toMillis(timeout), true);
    }

    public BillyFuture awaitUninterruptibly() {
        try {
            await_(Long.MAX_VALUE, false);
        } catch (InterruptedException e) {
            // do nothing
        }

        return this;
    }

    public boolean awaitUninterruptibly(long timeout, TimeUnit timeUnit) {
        try {
            return await_(timeUnit.toMillis(timeout), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        synchronized (lock) {
            return completed;
        }
    }

    public boolean setResult(Object newValue) {
        synchronized (lock) {
            // 仅允许在没有完成之前设置result.
            if (completed) {
                return false;
            }

            result = newValue;
            completed = true;

            if (waitCount > 0) {
                lock.notifyAll();
            }
        }

        notifyListeners();

        return true;
    }

    protected Object getResult() {
        synchronized (lock) {
            return result;
        }
    }


    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    private void notifyListener(FutureListener listener) {
        try {
            listener.operationComplete(this);
        } catch (Exception e) {
            // we should monitor this exception and deliver to business handler.
        }
    }

    private void notifyListeners() {
        for (FutureListener<?> listener : listeners) {
            notifyListener(listener);
        }
        listeners.clear();
    }

    private void checkDeadlock() {
        if (!needCheckDeadlock()) {
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 简单的检查
        for (StackTraceElement stackElement : stackTrace) {
            if (NioServerTemplete.class.getName().equals(stackElement.getClassName())) {
                IllegalStateException e = new IllegalStateException("t");
                e.printStackTrace();
                throw new IllegalStateException("DEAD LOCK: " + BillyFuture.class.getSimpleName() + ".await() was invoked from an I/O processor thread.  " + "Please use " + FutureListener.class.getSimpleName() + " or configure a proper thread model alternatively.");
            }
        }

        // 简单检查通过之后
        for (StackTraceElement stackElement : stackTrace) {
            try {
                Class<?> cls = FutureTemplate.class.getClassLoader().loadClass(stackElement.getClassName());

                if (Processor.class.isAssignableFrom(cls)) {
                    throw new IllegalStateException("DEAD LOCK: " + BillyFuture.class.getSimpleName() + ".await() was invoked from an I/O processor thread.  " + "Please use " + FutureListener.class.getSimpleName() + " or configure a proper thread model alternatively.");
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }
    }

    private boolean await_(long timeoutMillis, boolean interruptable) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeoutMillis;

        if (endTime < 0) {
            endTime = Long.MAX_VALUE;
        }

        synchronized (lock) {
            // 当future完成了或者超时了，我们都可以退出了
            if (completed||(timeoutMillis <= 0)) {
                return completed;
            }

            // The operation is not completed : we have to wait
            waitCount++;

            try {
                for (;;) {
                    try {
                        long timeOut = Math.min(timeoutMillis, DEADLOCK_CHECK_INTERVAL);

                        // 线程wait
                        // 但是每隔DEADLOCK_CHECK_INTERVAL的时间，我们都会让线程继续往下走进行一次死锁检查，而不会一直死wait在这一步
                        lock.wait(timeOut);
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        }
                    }

                    if (completed || (endTime < System.currentTimeMillis())) {
                        return completed;
                    } else {
                        // 检查死锁
                        checkDeadlock();
                    }
                }
            } finally {
                // 到这一步会有三种可能 :
                // 1) 我们被唤醒了 (操作被当前future或者其他方式完成了)
                // 2) 超时了
                // 3) 线程被interrupted
                waitCount--;

                if (!completed) {
                    checkDeadlock();
                }
            }
        }
    }

    /**
     * 不同的future实现是不一定需要检查死锁的。
     * 在billy里面，只有ReadFuture，WriteFuture，CloseFuture以及ConnectFuture会有可能导致死锁
     * @return
     */
    protected abstract boolean needCheckDeadlock();
}
