package cn.zjnktion.billy.context;

import cn.zjnktion.billy.filter.DefaultFilterChain;
import cn.zjnktion.billy.filter.FilterChain;
import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.service.Engine;

import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

/**
 * Created by zjnktion on 2016/4/3.
 */
public abstract class NioContextTemplate extends ContextTemplete {

    protected final Processor<NioContextTemplate> processor;

    protected final Channel channel;
    private SelectionKey key;

    private final FilterChain filterChain;

    protected NioContextTemplate(Engine engine, Processor<NioContextTemplate> processor, Channel channel) {
        super(engine);

        this.processor = processor;
        this.channel = channel;

        filterChain = new DefaultFilterChain(this);
    }

    public final Processor<NioContextTemplate> getProcessor() {
        return processor;
    }

    public final FilterChain getFilterChain() {
        return filterChain;
    }

    public final SelectionKey getSelectionKey() {
        return key;
    }

    public final void setSelectionKey(SelectionKey key) {
        this.key = key;
    }

    public final boolean isActive() {
        return key.isValid();
    }

    protected abstract ByteChannel getChannel();

}
