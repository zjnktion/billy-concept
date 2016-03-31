package cn.zjnktion.billy.service;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.context.ContextDataFactory;
import cn.zjnktion.billy.context.DefaultContextDataFactory;
import cn.zjnktion.billy.filter.DefaultFilterChainBuilder;
import cn.zjnktion.billy.filter.FilterChainBuilder;
import cn.zjnktion.billy.handler.Handler;
import cn.zjnktion.billy.listener.DefaultEngineListener;
import cn.zjnktion.billy.listener.EngineListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * {@link Engine} 的一个模板类
 * 通用引擎模板
 * Created by zhengjn on 2016/3/30.
 */
public abstract class EngineTemplate implements Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineTemplate.class);

    // maybe arraylist is better?
    private final List<EngineListener> listeners = new CopyOnWriteArrayList<EngineListener>();

    private Handler handler;

    private final Map<Long, Context> managedContexts = new ConcurrentHashMap<Long, Context>();;

    private ContextDataFactory contextDataFactory;
    protected final ContextConfig contextConfig;

    private FilterChainBuilder filterChainBuilder;

    private final Object disposeLock = new Object();
    private volatile boolean disposing;
    private volatile boolean disposed;

    private volatile boolean activated;

    private final Executor executor;
    private volatile boolean createdDefaultExecutor;

    protected EngineTemplate(ContextConfig contextConfig, Executor executor) {
        if (contextConfig == null) {
            throw new IllegalArgumentException("There's no context config to be assigned.");
        }

        if (getEngineMetainfo() == null) {
            throw new IllegalArgumentException("There's no engine meta info to be assigned.");
        }

        EngineListener defaultEngineListener = new DefaultEngineListener();
        listeners.add(defaultEngineListener);

        this.contextDataFactory = new DefaultContextDataFactory();
        this.contextConfig = contextConfig;

        if (executor == null) {
            this.executor = Executors.newCachedThreadPool();
            createdDefaultExecutor = true;
        } else {
            this.executor = executor;
            createdDefaultExecutor = false;
        }
    }

    protected void fireEngineActivated() {
        if (!activated) {
            activated = true;
            for (EngineListener listener : listeners) {
                try {
                    listener.engineActivated(this);
                } catch (Exception e) {
                    // we should monitor this exception and deliver to business handler.
                }
            }
        }
    }

    protected void fireEngineDeacitvated() {
        if (activated) {
            try {
                activated = false;
                for (EngineListener listener : listeners) {
                    try {
                        listener.engineDeactivated(this);
                    } catch (Exception e) {
                        // we should monitor this exception and deliver to business handler.
                    }
                }
            } finally {
                disconnectAllConnections();
            }
        }
    }

    public final void addListener(EngineListener listener) {
        listeners.add(listener);
    }

    public final void removeListener(EngineListener listener) {
        listeners.remove(listener);
    }

    protected List<EngineListener> getListeners() {
        return listeners;
    }

    public final Handler getHandler() {
        return handler;
    }

    public final void setHandler(Handler handler){
        if (handler == null) {
            throw new IllegalArgumentException("Cannot set a null handler.");
        }

        if (isAcitve()) {
            throw new IllegalStateException("Cannot set a handler while the engine is activated");
        }

        this.handler = handler;
    }

    public final Map<Long, Context> getManagedContexts() {
        return managedContexts;
    }

    public final int getManagedContextsCount() {
        return managedContexts.size();
    }

    public final ContextDataFactory getContextDataFactory() {
        return contextDataFactory;
    }

    public final void setContextDataFactory(ContextDataFactory contextDataFactory) {
        if (contextDataFactory == null) {
            throw new IllegalArgumentException("Cannot set a null context data factory.");
        }

        if (isAcitve()) {
            throw new IllegalStateException("Cannot set a context data factory while engine is active.");
        }

        this.contextDataFactory = contextDataFactory;
    }

    /**
     * 服务端和客户端有不同的实现
     * @param context
     */
    protected abstract void fireContextCreated(Context context);

    /**
     * 服务端和客户端有不同的实现
     * @param context
     */
    protected abstract void fireContextDestroyed(Context context);

    /**
     * 服务端和客户端有不同的实现
     */
    protected abstract void disconnectAllConnections();

    public FilterChainBuilder getFilterChainBuilder() {
        return filterChainBuilder;
    }

    public void setFilterChainBuilder(FilterChainBuilder builder) {
        if (builder == null) {
            builder = new DefaultFilterChainBuilder();
        }
        filterChainBuilder = builder;
    }

    public DefaultFilterChainBuilder getDefaultFilterChainBuilder() {
        if (!(filterChainBuilder instanceof DefaultFilterChainBuilder)) {
            throw new IllegalStateException("Current filter chain builder is not a DefaultFilterChainBuilder.");
        }
        return (DefaultFilterChainBuilder) filterChainBuilder;
    }

    public final boolean isAcitve() {
        return activated;
    }

    public final void dispose() {
        dispose(false);
    }

    public final void dispose(boolean immediately) {
        if (disposed) {
            return;
        }

        synchronized (disposeLock) {
            if (!disposing) {
                disposing = true;
            }

            try {
                disposeImpl();
            } catch (Exception e) {
                // we should monitor this exception and deliver to business handler.
            }
        }

        if (createdDefaultExecutor) {
            ExecutorService es = (ExecutorService) executor;
            es.shutdown();

            if (!immediately) {
                try {
                    LOGGER.debug("ExecutorService awaitTermination is called on {} by Thread[{}]", this, Thread.currentThread().getName());
                    es.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                    LOGGER.debug("ExecutorService termination success on {} by Thread[{}]", this, Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    LOGGER.warn("ExecutorService awaitTermination cause InterruptedException on {} by [{}]", this, Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                }
            }
        }

        disposed = true;
    }

    /**
     * 服务端和客户端有不同的实现
     * @throws Exception
     */
    protected abstract void disposeImpl() throws Exception;

    public final boolean isDisposing() {
        return disposing;
    }

    public final boolean isDisposed() {
        return disposed;
    }
}
