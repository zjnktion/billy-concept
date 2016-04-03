package cn.zjnktion.billy.context;

import cn.zjnktion.billy.common.IdleType;
import cn.zjnktion.billy.filter.FilterChain;
import cn.zjnktion.billy.future.CloseFuture;
import cn.zjnktion.billy.future.ReadFuture;
import cn.zjnktion.billy.future.WriteFuture;
import cn.zjnktion.billy.service.Engine;
import cn.zjnktion.billy.task.WriteTask;
import cn.zjnktion.billy.task.WriteTaskQueue;

import java.net.SocketAddress;

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

    /**
     * 异步读
     * 对于服务器而言，其实是不建议使用异步读的，如果有大量的客户端发起业务请求，读队列将会导致意想不到的内存溢出问题
     * 对于客户端而已，可以开启异步读
     * 所以该操作需要在{@link ContextConfig}中配置生效才能使用，否则将会跑出{@link IllegalStateException}
     * 服务器默认读操作会在所有filter执行后同步执行，如果该项没有配置开启的话
     * @return
     */
    ReadFuture read();

    void suspendRead();

    void resumeRead();

    boolean isReadSuspended();

    WriteFuture write(Object message);

    WriteFuture write(Object message, SocketAddress destination);

    void suspendWrite();

    void resumeWrite();

    boolean isWriteSuspended();

    WriteTaskQueue getWriteTaskQueue();

    WriteTask getCurrentWriteTask();

    void setCurrentWriteTask(WriteTask currentWriteTask);

    Object getCurrentWriteMessage();

    CloseFuture closeImmediately();

    CloseFuture closeOnFlush();

    CloseFuture getCloseFuture();

    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();

    boolean isConnected();

    boolean isActive();

    long getLastIoTime();

    long getLastReadTime();

    long getLastWriteTime();

    boolean isIdle(IdleType idleType);

    long getLastIdleTime(IdleType idleType);

    int getIdleCount(IdleType idleType);

    boolean isClosing();

    Object getAttr(String key);

    Object setAttr(String key, Object value);

    Object setAttrIfAbsent(String key, Object value);

    void removeAttr(String key);

    boolean containsAttr(String key);
}
