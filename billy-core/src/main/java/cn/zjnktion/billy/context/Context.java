package cn.zjnktion.billy.context;

import cn.zjnktion.billy.filter.FilterChain;

/**
 * Billy上下文
 * Created by zhengjn on 2016/3/30.
 */
public interface Context {

    long getId();

    FilterChain getFilterChain();

}
