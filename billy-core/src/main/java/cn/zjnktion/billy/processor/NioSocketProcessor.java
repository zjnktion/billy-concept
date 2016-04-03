package cn.zjnktion.billy.processor;

import cn.zjnktion.billy.context.NioSocketContext;
import cn.zjnktion.billy.processor.exception.ProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zjnktion on 2016/4/2.
 */
public class NioSocketProcessor extends NioProcessorTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioSocketProcessor);

    private Selector selector;
    private SelectorProvider selectorProvider = null;

    private long lastIdleCheckTime;
    private static final long SELECT_TIMEOUT = 1000L;
    private AtomicBoolean wakeupCalled = new AtomicBoolean(false);

    private AtomicReference<Worker> workerRef = new AtomicReference<Worker>();

    public NioSocketProcessor(Executor executor) {
        super(executor);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new ProcessorException("Failed to open a selector.", e);
        }
    }

    public NioSocketProcessor(Executor executor, SelectorProvider selectorProvider) {
        super(executor);

        try {
            if (selectorProvider == null) {
                selector = Selector.open();
            } else {
                this.selectorProvider = selectorProvider;
                selector = selectorProvider.openSelector();
            }
        } catch (IOException e) {
            throw new ProcessorException("Failed to open a selector.", e);
        }
    }

    private class Worker implements Runnable {

        public void run() {
            assert (workerRef.get() == this);

            int contextCount = 0;
            lastIdleCheckTime = System.currentTimeMillis();

            while(true) {
                try {
                    long selectStartTime = System.currentTimeMillis();
                    int selected = selector.select(SELECT_TIMEOUT);
                    long selectEndTime = System.currentTimeMillis();
                    long dealTime = selectEndTime - selectStartTime;

                    if (!wakeupCalled.getAndSet(false) && selected == 0 && dealTime < 100L) {
                        if (isBroken()) {
                            LOGGER.warn("Connection has been broken.");
                        } else {
                            LOGGER.warn("Create a new select because selected is 0 in deal time = {} millis", dealTime);

                            // 通常这个操作是由于java原生nio的一个很严苛的竟态条件才会产生的。
                            // 一旦触发了这个竟态条件，cpu占用率将会出现不可预料的100%。
                            // 为了解决这个竟态条件，找了很多攻略，一般都是采用新建selector并且关闭旧的selector的方法。
                            newSelector();
                        }
                    }

                    contextCount += addNewSessions();
                }
            }
        }
    }

    private boolean isBroken() throws IOException {
        boolean broken = false;

        synchronized (selector) {
            Set<SelectionKey> keys = selector.keys();

            for (SelectionKey key : keys) {
                SelectableChannel channel = key.channel();
                if (!((SocketChannel) channel).isConnected()) {
                    key.cancel();
                    broken = true;
                }
            }
        }

        return broken;
    }

    private void newSelector() throws IOException {
        synchronized (selector) {
            Set<SelectionKey> keys = selector.keys();

            Selector newSelector = null;

            // 开启新的selector
            if (selectorProvider == null) {
                newSelector = Selector.open();
            } else {
                newSelector = selectorProvider.openSelector();
            }

            // 关联key
            for (SelectionKey key : keys) {
                SelectableChannel channel = key.channel();

                NioSocketContext context = (NioSocketContext) key.attachment();
                // 设置attr
                SelectionKey newKey = channel.register(newSelector, key.interestOps(), context);
                context.setSelectionKey(newKey);
            }

            // 关闭旧的selector
            selector.close();

            selector = newSelector;
        }
    }

}
