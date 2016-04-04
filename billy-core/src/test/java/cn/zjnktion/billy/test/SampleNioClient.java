package cn.zjnktion.billy.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by zhengjn on 2016/3/11.
 */
public class SampleNioClient {

    private Selector selector;
    private SocketChannel socketChannel;

    private SampleNioClient() {

    }

    public static SampleNioClient open() throws IOException {
        SampleNioClient client = new SampleNioClient();
        client.selector = Selector.open();
        client.socketChannel = SocketChannel.open();
        client.socketChannel.configureBlocking(false);
        return client;
    }

    public void connect(int port) throws IOException {
        socketChannel.connect(new InetSocketAddress("127.0.0.1", port));
        socketChannel.register(this.selector, SelectionKey.OP_CONNECT);
        System.out.println("client connect success");
        while (true) {
            this.selector.select();
            Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
            while (itr.hasNext()) {
                SelectionKey key = itr.next();
                itr.remove();
                process(key);
            }
        }
    }

    protected void process(SelectionKey key) throws IOException {
        if (key.isConnectable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            channel.configureBlocking(false);
            System.out.println("client finish connect");
            channel.write(ByteBuffer.wrap("hello".getBytes()));
            channel.register(this.selector, SelectionKey.OP_READ);
        }
        else if (key.isReadable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            System.out.println(channel.getRemoteAddress() + " readable...");
            ByteBuffer buff = ByteBuffer.allocate(1024);
            try {
                channel.read(buff);
            }
            catch (IOException e) {
                throw e;
            }
            System.out.println(new String(buff.array()));
        }
        else if (key.isWritable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            System.out.println(channel.getRemoteAddress() + " writable...");
        }
    }

}