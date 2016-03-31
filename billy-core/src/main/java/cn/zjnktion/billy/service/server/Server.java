package cn.zjnktion.billy.service.server;

import cn.zjnktion.billy.service.Engine;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * I/O服务器
 * Created by zhengjn on 2016/3/30.
 */
public interface Server extends Engine {

    SocketAddress getBoundAddress();

    void setBoundAddress(SocketAddress boundAddress);

    void bind() throws IOException;

    void bind(SocketAddress socketAddress) throws IOException;

    boolean isCloseContextsBeforeUnbind();

    void setCloseContextsBeforeUnbind(boolean closeContextsBeforeUnbind);

    void unbind() throws IOException;

}
