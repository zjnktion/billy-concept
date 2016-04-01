package cn.zjnktion.billy.context;

import cn.zjnktion.billy.filter.FilterChain;
import cn.zjnktion.billy.future.CloseFuture;
import cn.zjnktion.billy.future.ReadFuture;
import cn.zjnktion.billy.future.WriteFuture;
import cn.zjnktion.billy.service.Engine;
import cn.zjnktion.billy.task.WriteTaskQueue;

/**
 * Billy上下文
 * Created by zhengjn on 2016/3/30.
 */
public interface Context {

    long getId();

    Engine getEngine();

    /**
     * 每个session都有属于自己的FilterCain，区别于整个引擎的FilterChain
     * 实现个性化
     * @return
     */
    FilterChain getFilterChain();

    ReadFuture read();

    WriteFuture write(Object message);

    WriteTaskQueue getWriteTaskQueue();

    CloseFuture closeImmediately();

    CloseFuture closeOnFlush();
}
