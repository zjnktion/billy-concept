package cn.zjnktion.billy.task;

import cn.zjnktion.billy.context.Context;

/**
 * Created by zhengjn on 2016/4/1.
 */
public interface WriteTaskQueue {

    Context getContext();

    void offer(WriteTask writeTask);

    WriteTask poll();

    int size();

    boolean isEmpty();

    void clear();

    void dispose();

}
