package cn.zjnktion.billy.processor;

import java.util.concurrent.Executor;

/**
 * Created by zjnktion on 2016/4/2.
 */
public abstract class NioSocketProcessor extends NioProcessorTemplate {

    protected NioSocketProcessor(Executor executor) {
        super(executor);
    }
}
