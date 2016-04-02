package cn.zjnktion.billy.processor;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.task.WriteTask;

/**
 * Created by zhengjn on 2016/3/31.
 */
public interface Processor<C extends Context> {

    void add(C context);

    void remove(C context);

    void write(C context, WriteTask writeTask);

    void flush(C context);

    void updateSuspendStatus(C context);

    void dispose();

    boolean isDisposing();

    boolean isDisposed();

}
