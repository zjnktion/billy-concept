package cn.zjnktion.billy.task;

import cn.zjnktion.billy.future.WriteFuture;

import java.net.SocketAddress;

/**
 * Created by zhengjn on 2016/4/1.
 */
public interface WriteTask {

    WriteTask getOriginalTask();

    boolean isEncoded();

    Object getMessage();

    WriteFuture getFuture();

    SocketAddress getDestination();

}
