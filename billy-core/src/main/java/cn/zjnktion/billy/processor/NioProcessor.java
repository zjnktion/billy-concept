package cn.zjnktion.billy.processor;

import cn.zjnktion.billy.processor.exception.ProcessorException;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;

/**
 * Created by zjnktion on 2016/4/2.
 */
public class NioProcessor extends ProcessorTemplate {

    private Selector selector;
    private SelectorProvider selectorProvider = null;

    public NioProcessor(Executor executor) {
        super(executor);

        try {
            selector = Selector.open();
        }
        catch (IOException e) {
            throw new ProcessorException("Failed to open a selector.", e);
        }
    }

    public NioProcessor(Executor executor, SelectorProvider selectorProvider) {
        super(executor);

        try {
            if (selectorProvider == null) {
                selector = Selector.open();
            }
            else {
                this.selectorProvider = selectorProvider;
                selector = selectorProvider.openSelector();
            }
        }
        catch (IOException e) {
            throw new ProcessorException("Failed to open a selector.", e);
        }
    }

}
