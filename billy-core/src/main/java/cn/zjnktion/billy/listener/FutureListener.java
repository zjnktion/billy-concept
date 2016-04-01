package cn.zjnktion.billy.listener;

import cn.zjnktion.billy.future.BillyFuture;

import java.util.EventListener;

/**
 * Created by zjnktion on 2016/3/31.
 */
public interface FutureListener<F extends BillyFuture> extends EventListener {

    void operationComplete(F future);

}
