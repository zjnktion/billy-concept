package cn.zjnktion.billy.service.server;

import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.context.ContextTemplete;
import cn.zjnktion.billy.processor.DefaultProcessorPool;
import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.processor.ProcessorPool;
import cn.zjnktion.billy.service.server.exception.ServerInitException;

import java.util.concurrent.Executor;

/**
 * Created by zhengjn on 2016/3/31.
 */
public abstract class ConnectionOrientedServerTemplete<C extends ContextTemplete, S> extends ServerTemplate {

    private final ProcessorPool<C> processorPool;
    private final boolean createdDefaultProcessorPool;

    private volatile boolean acceptable;

    private S socket;

    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, Class<? extends Processor<C>> processorClass) {
        this(contextConfig, null, new DefaultProcessorPool<C>(processorClass), true);
    }

    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, ProcessorPool<C> processorPool) {
        this(contextConfig, null, processorPool, false);
    }

    protected ConnectionOrientedServerTemplete(ContextConfig contextConfig, Executor executor, ProcessorPool<C> processorPool) {
        this(contextConfig, executor, processorPool, false);
    }

    private ConnectionOrientedServerTemplete(ContextConfig contextConfig, Executor executor, ProcessorPool<C> processorPool, boolean createdDefaultProcessorPool) {
        super(contextConfig, executor);

        if (processorPool == null) {
            throw new IllegalArgumentException("Cannot set a null processor pool.");
        }

        this.processorPool = processorPool;
        this.createdDefaultProcessorPool = createdDefaultProcessorPool;

        try {
            initServer();
            acceptable = true;
        }
        catch (Exception e) {
            throw new ServerInitException("Failed to initialize " + getClass().getSimpleName() + ".", e);
        }
        finally {
            if (!acceptable) {
                try {
                    destoryServer();
                } catch (Exception e) {
                    // we should monitor this exception and deliver to business handler.
                }
            }
        }
    }

}
