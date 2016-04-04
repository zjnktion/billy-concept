package cn.zjnktion.billy.context;

import cn.zjnktion.billy.common.IdleType;

import java.util.concurrent.TimeUnit;

/**
 * Billy上下文配置
 * Created by zhengjn on 2016/3/30.
 */
public interface ContextConfig {

    int getReadBufferSize();

    void setReadBufferSize(int size);

    int getMinReadBufferSize();

    void setMinReadBufferSize(int size);

    int getMaxReadBufferSize();

    void setMaxReadBufferSize(int size);

    long getIdleTime(IdleType idleType, TimeUnit timeUnit);

    void setIdleTime(IdleType idleType, TimeUnit timeUnit);

    void setConfig(ContextConfig config);

    /**
     * 该项配置是默认关闭的
     * @return
     */
    boolean isAsyncReadEnable();
}
