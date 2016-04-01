package cn.zjnktion.billy.service.server;

import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.context.ContextTemplete;
import cn.zjnktion.billy.future.ServerOperationFuture;
import cn.zjnktion.billy.processor.DefaultProcessorPool;
import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.processor.ProcessorPool;
import cn.zjnktion.billy.service.server.exception.ServerInitException;

import java.net.SocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * Created by zhengjn on 2016/3/31.
 */
public abstract class ConnectionOrientedServerTemplete<C extends ContextTemplete, S> extends ServerTemplate {

    private final ProcessorPool<C> processorPool;
    private final boolean createdDefaultProcessorPool;

    protected final SelectorProvider selectorProvider;

    private volatile boolean acceptable;

    private final Queue<ServerOperationFuture> bindQueue = new ConcurrentLinkedQueue<ServerOperationFuture>();
    private final Queue<ServerOperationFuture> unbindQueue = new ConcurrentLinkedQueue<ServerOperationFuture>();

    private S serverSocket;

    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, Class<? extends Processor<C>> processorClass) {
        this(contextConfig, null, new DefaultProcessorPool<C>(processorClass), true, null);
    }

    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, Class<? extends Processor<C>> processorClass, SelectorProvider selectorProvider) {
        this(contextConfig, null, new DefaultProcessorPool<C>(processorClass), true, selectorProvider);
    }


    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, ProcessorPool<C> processorPool) {
        this(contextConfig, null, processorPool, false, null);
    }

    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, Executor executor, ProcessorPool<C> processorPool) {
        this(contextConfig, executor, processorPool, false, null);
    }

    private ConnectionOrientedServerTemplete(ContextConfig contextConfig, Executor executor, ProcessorPool<C> processorPool, boolean createdDefaultProcessorPool, SelectorProvider selectorProvider) {
        super(contextConfig, executor);

        if (processorPool == null) {
            throw new IllegalArgumentException("Cannot set a null processor pool.");
        }

        this.processorPool = processorPool;
        this.createdDefaultProcessorPool = createdDefaultProcessorPool;

        this.selectorProvider = selectorProvider;

        try {
            init();
            acceptable = true;
        }
        catch (Exception e) {
            throw new ServerInitException("Failed to initialize " + getClass().getSimpleName() + ".", e);
        }
        finally {
            if (!acceptable) {
                try {
                    destory();
                } catch (Exception e) {
                    // we should monitor this exception and deliver to business handler.
                }
            }
        }
    }

    protected final SocketAddress bindImpl(SocketAddress socketAddress) throws Exception {
        ServerOperationFuture serverOperationFuture = new ServerOperationFuture(socketAddress);

        bindQueue.add(serverOperationFuture);
        return null;
    }

    protected final void unbindImpl() throws Exception {

    }

    protected abstract void init() throws Exception;

    protected abstract void destory() throws Exception;

}
