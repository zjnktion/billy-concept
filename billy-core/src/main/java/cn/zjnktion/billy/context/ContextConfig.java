package cn.zjnktion.billy.context;

import cn.zjnktion.billy.common.IdleType;

/**
 * Billy上下文配置
 * Created by zhengjn on 2016/3/30.
 */
public interface ContextConfig {

    /**
     * 该项配置是默认关闭的
     * @return
     */
    boolean isAsyncReadEnable();

    int getIdleTime(IdleType idleType);
}
