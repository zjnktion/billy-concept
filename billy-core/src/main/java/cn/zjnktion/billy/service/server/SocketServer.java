package cn.zjnktion.billy.service.server;

/**
 * 基于Socket的I/O服务器
 * Created by zhengjn on 2016/3/30.
 */
public interface SocketServer extends Server {

    boolean isKeepAlive();

    void setKeepAlive(boolean keepAlive);

    boolean isReuseAddress();

    void setReuseAddress(boolean reuseAddress);

    int getBacklog();

    void setBacklog(int backlog);

    boolean isTcpNoDelay();

    void setTcpNoDelay(boolean tcpNoDelay);

    int getSendBufferSize();

    void setSendBufferSize(int sendBufferSize);

    int getReceiveBufferSize();

    void setReceiveBufferSize(int receiveBufferSize);

}
