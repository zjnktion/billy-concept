package cn.zjnktion.billy.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * An easy sample as java nio server.
 * Created by zhengjn on 2016/3/11.
 */
public class SampleNioServer {

    private SampleNioServer() {

    }

    private Selector selector;
    private ServerSocketChannel serverChannel;

    public static SampleNioServer open() throws IOException {
        SampleNioServer server = new SampleNioServer();
        server.selector = Selector.open();
        server.serverChannel = ServerSocketChannel.open();
        server.serverChannel.configureBlocking(true);
        return server;
    }

    public void bind(int port) throws IOException {
        this.serverChannel.bind(new InetSocketAddress(port));
        this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        System.out.println("server binding success");
        while (true) {
            selector.select(5000);
            Iterator<SelectionKey> keyIterator = selector.keys().iterator();
            while (keyIterator.hasNext()) {
                System.out.println(keyIterator.next().interestOps());
            }
            Iterator<SelectionKey> itr = this.selector.selectedKeys().iterator();
            while (itr.hasNext()) {
                SelectionKey key = itr.next();
                itr.remove();
                process(key);
            }
        }
    }

    public void bind0(int port) throws IOException {
        this.serverChannel.bind(new InetSocketAddress(port));
        SocketChannel channel = this.serverChannel.accept();
        System.out.println(channel.getRemoteAddress());
    }

    protected void process(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            channel.register(this.selector, SelectionKey.OP_WRITE);
            channel.register(this.selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            System.out.println(channel.getRemoteAddress() + " readable...");
            ByteBuffer buff = ByteBuffer.allocate(1024);
            try {
                channel.read(buff);
            } catch (IOException e) {
                //e.printStackTrace();
                throw e;
            }
            System.out.println(new String(buff.array()));
            //channel.register(this.selector, SelectionKey.OP_WRITE);
        } else if (key.isWritable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            System.out.println(channel.getRemoteAddress() + " writable...");
            channel.write(ByteBuffer.wrap("shello".getBytes()));
        }
    }

}