package cn.zjnktion.billy.service;

import cn.zjnktion.billy.common.TransportMetadata;
import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.context.ContextDataFactory;
import cn.zjnktion.billy.filter.FilterChainBuilder;
import cn.zjnktion.billy.handler.Handler;
import cn.zjnktion.billy.listener.EngineListener;

import java.util.Map;

/**
 * Billy核心接口
 * Created by zhengjn on 2016/3/30.
 */
public interface Engine {

    TransportMetadata getEngineMetainfo();

    void addListener(EngineListener listener);

    void removeListener(EngineListener listener);

    Handler getHandler();

    void setHandler(Handler handler);

    Map<Long, Context> getManagedContexts();

    int getManagedContextsCount();

    ContextDataFactory getContextDataFactory();

    void setContextDataFactory(ContextDataFactory contextDataFactory);

    ContextConfig getContextConfig();

    /**
     * 通过builder模式隐藏FilterChain构建
     * @return
     */
    FilterChainBuilder getFilterChain();

    /**
     * 用户自定义FilterChain的builder
     * @param builder
     */
    void setFilterChainBuilder(FilterChainBuilder builder);

    boolean isAcitve();

    void dispose();

    void dispose(boolean immediately);

    boolean isDisposing();

    boolean isDisposed();

}
