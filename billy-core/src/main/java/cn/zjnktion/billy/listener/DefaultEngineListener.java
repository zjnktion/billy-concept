package cn.zjnktion.billy.listener;

import cn.zjnktion.billy.common.IdleType;
import cn.zjnktion.billy.context.Context;
import cn.zjnktion.billy.service.Engine;

/**
 * Created by zhengjn on 2016/3/30.
 */
public class DefaultEngineListener implements EngineListener {
    public void engineActivated(Engine engine) throws Exception {
        // empty
    }

    public void engineIdle(Engine engine, IdleType idleType) throws Exception {
        // empty
    }

    public void engineDeactivated(Engine engine) throws Exception {
        // empty
    }

    public void contextCreated(Context context) throws Exception {
        // empty
    }

    public void contextClosed(Context context) throws Exception {
        // empty
    }

    public void contextDestroyed(Context context) throws Exception {
        // empty
    }
}
