package cn.zjnktion.billy.service.server;

import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.context.ContextTemplete;
import cn.zjnktion.billy.future.ServerOperationFuture;
import cn.zjnktion.billy.processor.DefaultProcessorPool;
import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.processor.ProcessorPool;
import cn.zjnktion.billy.service.server.exception.ServerInitException;

import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhengjn on 2016/3/31.
 */
public abstract class NioServerTemplete<C extends ContextTemplete, S> extends ServerTemplate {

    protected final ProcessorPool<C> processorPool;
    protected final boolean createdDefaultProcessorPool;

    protected final SelectorProvider selectorProvider;
    protected volatile Selector selector;

    // 当前server是否可以开始工作了
    protected volatile boolean workable;
    protected final Semaphore semaphore = new Semaphore(1);

    protected final Queue<ServerOperationFuture> registerQueue = new ConcurrentLinkedQueue<ServerOperationFuture>();
    protected final Queue<ServerOperationFuture> unregisterQueue = new ConcurrentLinkedQueue<ServerOperationFuture>();

    protected volatile S serverChannel;

    protected NioServerTemplete(ContextConfig contextConfig, Class<? extends Processor<C>> processorClass) {
        this(contextConfig, null, new DefaultProcessorPool<C>(processorClass), true, null);
    }

    protected NioServerTemplete(ContextConfig contextConfig, Class<? extends Processor<C>> processorClass, SelectorProvider selectorProvider) {
        this(contextConfig, null, new DefaultProcessorPool<C>(processorClass), true, selectorProvider);
    }


    protected NioServerTemplete(ContextConfig contextConfig, ProcessorPool<C> processorPool) {
        this(contextConfig, null, processorPool, false, null);
    }

    protected NioServerTemplete(ContextConfig contextConfig, Executor executor, ProcessorPool<C> processorPool) {
        this(contextConfig, executor, processorPool, false, null);
    }

    private NioServerTemplete(ContextConfig contextConfig, Executor executor, ProcessorPool<C> processorPool, boolean createdDefaultProcessorPool, SelectorProvider selectorProvider) {
        super(contextConfig, executor);

        if (processorPool == null) {
            throw new IllegalArgumentException("Cannot set a null processor pool.");
        }

        this.processorPool = processorPool;
        this.createdDefaultProcessorPool = createdDefaultProcessorPool;

        this.selectorProvider = selectorProvider;

        try {
            init();
            workable = true;
        }
        catch (Exception e) {
            throw new ServerInitException("Failed to initialize " + getClass().getSimpleName() + ".", e);
        }
        finally {
            if (!workable) {
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
        registerQueue.add(serverOperationFuture);

        runWorker();

        try {
            semaphore.acquire();

            TimeUnit.MILLISECONDS.sleep(10);
            wakeupSelect();
        } finally {
            semaphore.release();
        }

        serverOperationFuture.awaitUninterruptibly();

        if (serverOperationFuture.getCause() != null) {
            throw serverOperationFuture.getCause();
        }

        return localAddress(serverChannel);
    }

    protected final void unbindImpl() throws Exception {
        ServerOperationFuture serverOperationFuture = new ServerOperationFuture(getBoundAddress());
        unregisterQueue.add(serverOperationFuture);

        runWorker();
        wakeupSelect();

        serverOperationFuture.awaitUninterruptibly();

        if (serverOperationFuture.getCause() != null) {
            throw serverOperationFuture.getCause();
        }
    }

    protected final void disposeImpl() throws Exception {
        unbind();
        runWorker();
        wakeupSelect();
    }

    protected final void init() throws Exception {
        if (selectorProvider == null) {
            selector = Selector.open();
        } else {
            selector = selectorProvider.openSelector();
        }
    }

    protected final void destory() throws Exception {
        if (selector != null) {
            selector.close();
        }
    }

    protected final int select() throws Exception {
        return selector.select();
    }

    protected final int select(long timeout) throws Exception {
        return selector.select(timeout);
    }

    protected final void wakeupSelect() {
        selector.wakeup();
    }

    /**
     * 面向连接和无连接有不同的实现
     * @return
     */
    protected abstract void runWorker() throws InterruptedException;

    /**
     * 面向连接和无连接有不同的实现
     * @return
     */
    protected abstract SocketAddress localAddress(S serverChannel) throws Exception;

    /**
     * 面向连接和无连接有不同的实现
     * @return
     */
    protected abstract S open();

    /**
     * 面向连接和无连接有不同的实现
     * @return
     */
    protected abstract int register();

    /**
     * 面向连接和无连接有不同的实现
     * @return
     */
    protected abstract int unregister();

}
