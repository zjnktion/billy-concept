package cn.zjnktion.billy.service;

import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.context.ContextDataFactory;
import cn.zjnktion.billy.filter.DefaultFilterChainBuilder;
import cn.zjnktion.billy.filter.FilterChainBuilder;
import cn.zjnktion.billy.handler.Handler;
import cn.zjnktion.billy.listener.EngineListener;

import java.util.Map;

/**
 * Billy核心接口
 * Created by zhengjn on 2016/3/30.
 */
public interface Engine {

    EngineMetainfo getEngineMetainfo();

    void addListener(EngineListener listener);

    void removeListener(EngineListener listener);

    Handler getHandler();

    void setHandler(Handler handler);

    Map<Long, Context> getManagedContexts();

    int getManagedContextsCount();

    ContextDataFactory getContextDataFactory();

    void setContextDataFactory(ContextDataFactory contextDataFactory);

    ContextConfig getContextConfig();

    FilterChainBuilder getFilterChainBuilder();

    void setFilterChainBuilder(FilterChainBuilder builder);

    DefaultFilterChainBuilder getDefaultFilterChainBuilder();

    boolean isAcitve();

    void dispose();

    void dispose(boolean immediately);

    boolean isDisposing();

    boolean isDisposed();

}
