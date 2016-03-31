package cn.zjnktion.billy.listener;

import cn.zjnktion.billy.common.IdleType;
import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.service.Engine;

import java.util.EventListener;

/**
 * {@link Engine}事件监听器
 * Created by zhengjn on 2016/3/30.
 */
public interface EngineListener extends EventListener {

    void engineActivated(Engine engine) throws Exception;

    void engineIdle(Engine engine, IdleType idleType) throws Exception;

    void engineDeactivated(Engine engine) throws Exception;

    void contextCreated(Context context) throws Exception;

    void contextClosed(Context context) throws Exception;

}
