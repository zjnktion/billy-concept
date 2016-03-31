package cn.zjnktion.billy.processor;

import cn.zjnktion.billy.context.Context;

/**
 * Created by zhengjn on 2016/3/31.
 */
public class DefaultProcessorPool<C extends Context> implements ProcessorPool<C> {

    public DefaultProcessorPool(Class<? extends Processor<C>> processClass) {

    }

}
