package cn.zjnktion.billy.filter;

/**
 * Created by zhengjn on 2016/3/30.
 */
public interface FilterChain {

    void fireContextCreated();

    void fireContextOpened();

    void fireContextClosed();

    void fireExceptionCaught();

    void fireFilterWrite();

    void fireFilterClose();
}
