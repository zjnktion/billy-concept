package cn.zjnktion.billy.future;

import cn.zjnktion.billy.context.Context;

/**
 * Created by zhengjn on 2016/4/1.
 */
public class GenericFuture extends FutureTemplate {

    public GenericFuture(Context context) {
        super(context);
    }

    @Override
    protected boolean needCheckDeadlock() {
        return false;
    }
}
