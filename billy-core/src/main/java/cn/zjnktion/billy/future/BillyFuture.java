package cn.zjnktion.billy.future;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.listener.FutureListener;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhengjn on 2016/4/1.
 */
public interface BillyFuture extends Future {

    Context getContext();

    BillyFuture addListener(FutureListener<?> listener);

    BillyFuture removeListener(FutureListener<?> listener);

    BillyFuture await() throws InterruptedException;

    boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException;

    BillyFuture awaitUninterruptibly();

    boolean awaitUninterruptibly(long timeout, TimeUnit timeUnit);

}
