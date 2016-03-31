package cn.zjnktion.billy.service.server;

import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.service.EngineTemplate;
import cn.zjnktion.billy.service.server.exception.BindException;
import cn.zjnktion.billy.service.server.exception.UnbindException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

/**
 * 通用I/O服务器模板
 * Created by zhengjn on 2016/3/30.
 */
public abstract class ServerTemplate extends EngineTemplate implements Server {

    private SocketAddress boundAddress;

    private boolean closeContextBeforeUnbind = true;

    protected ServerTemplate(ContextConfig contextConfig, Executor executor) {
        super(contextConfig, executor);
    }

    public final SocketAddress getBoundAddress() {
        return boundAddress;
    }

    public final void setBoundAddress(SocketAddress socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("Cannot set a null bound address.");
        }

        if (isAcitve()) {
            throw new IllegalStateException("Cannot set bound address while server is activated.");
        }

        boundAddress = socketAddress;
    }

    public final void bind() throws IOException {
        bind(getDefaultLocalAddress());
    }

    public final void bind(SocketAddress socketAddress) throws IOException {
        if (socketAddress == null) {
            throw new IllegalArgumentException("Cannot bind a null address.");
        }

        if (isAcitve()) {
            throw new IllegalStateException("Cannot binding while server is activated.");
        }

        if (isDisposing()) {
            throw new IllegalStateException("Cannot binding while server is disposing.");
        }

        if (getHandler() == null) {
            throw new IllegalStateException("Cannot binding without set a handler.");
        }

        try {
            boundAddress = bindImpl(socketAddress);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new BindException("Failed to bind " + socketAddress + ".", e);
        }

        fireEngineActivated();
    }

    public final void unbind() throws IOException {
        if (!isAcitve()) {
            throw new IllegalStateException("Cannot unbinding while server is deactivated.");
        }

        if (isDisposing()) {
            throw new IllegalStateException("Cannot unbinding while server is disposing.");
        }

        try {
            unbindImpl(boundAddress);
            boundAddress = null;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new UnbindException("Failed to unbind " + boundAddress + ".", e);
        }

        fireEngineDeacitvated();
    }

    /**
     * 不同的io类型有不同的实现
     * @param socketAddress
     * @return
     * @throws Exception
     */
    protected abstract SocketAddress bindImpl(SocketAddress socketAddress) throws Exception;

    /**
     * 不同的io有不同的实现
     * @param socketAddress
     * @throws Exception
     */
    protected abstract void unbindImpl(SocketAddress socketAddress) throws Exception;

    /**
     * Billy默认服务监听Address
     * @return
     */
    private SocketAddress getDefaultLocalAddress() {
        if (boundAddress != null) {
            return boundAddress;
        }
        return new InetSocketAddress(5222);
    }

}
