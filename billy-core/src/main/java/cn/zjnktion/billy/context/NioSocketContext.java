package cn.zjnktion.billy.context;

import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.service.Engine;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Created by zjnktion on 2016/4/3.
 */
public final class NioSocketContext extends NioContextTemplate {

    public NioSocketContext(Engine engine, Processor<NioContextTemplate> processor, SocketChannel channel) {
        super(engine, processor, channel);
    }

    public final InetSocketAddress getLocalAddress() {
        if (channel == null) {
            return null;
        }

        Socket socket = getSocket();
        if (socket == null) {
            return null;
        }

        return (InetSocketAddress) socket.getLocalSocketAddress();
    }

    public final InetSocketAddress getRemoteAddress() {
        if (channel == null) {
            return null;
        }

        Socket socket = getSocket();
        if (socket == null) {
            return null;
        }

        return (InetSocketAddress) socket.getRemoteSocketAddress();
    }

    @Override
    public final SocketChannel getChannel() {
        return (SocketChannel) channel;
    }

    private Socket getSocket() {
        return ((SocketChannel) channel).socket();
    }

}
