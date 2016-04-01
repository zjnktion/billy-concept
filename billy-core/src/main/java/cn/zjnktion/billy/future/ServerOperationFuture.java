package cn.zjnktion.billy.future;

import java.net.SocketAddress;

/**
 * Created by zhengjn on 2016/4/1.
 */
public class ServerOperationFuture extends EngineOperationFuture {

    private final SocketAddress boundAddress;

    public ServerOperationFuture(SocketAddress socketAddress) {
        boundAddress = socketAddress;
    }

    public final SocketAddress getBoundAddress() {
        return boundAddress;
    }

    public String toString() {
        return "Server operation : " + boundAddress;
    }

}
