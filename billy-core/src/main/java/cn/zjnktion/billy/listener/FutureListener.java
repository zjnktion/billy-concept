package cn.zjnktion.billy.listener;

import java.util.EventListener;
import java.util.concurrent.Future;

/**
 * Created by zjnktion on 2016/3/31.
 */
public interface FutureListener<F extends Future> extends EventListener {

    void operationComplete(F future);

}
