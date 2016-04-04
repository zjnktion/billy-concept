package cn.zjnktion.billy.service.server;

import cn.zjnktion.billy.common.DefaultTransportMetadata;
import cn.zjnktion.billy.common.TransportMetadata;
import cn.zjnktion.billy.context.SocketContextConfig;

import java.net.InetSocketAddress;

/**
 * NIO服务器
 * Created by zhengjn on 2016/3/30.
 */
public abstract class NioSocketServer extends NioServerTemplete implements SocketServer {

    public static final TransportMetadata METADATA = new DefaultTransportMetadata(TransportMetadata.PROVIDER_NAMES.NIO.getValue(), TransportMetadata.TYPE_NAME.SOCKET.getValue(), false, true, InetSocketAddress.class, SocketContextConfig.class);

    public NioSocketServer() {
        super(null, NioSocketServer.class);
    }

    public TransportMetadata getTransportMetadata() {
        return METADATA;
    }

}
