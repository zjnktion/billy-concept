package cn.zjnktion.billy.processor;

import cn.zjnktion.billy.context.ContextTemplete;
import cn.zjnktion.billy.filter.FilterChain;
import cn.zjnktion.billy.future.GenericFuture;
import cn.zjnktion.billy.task.WriteTask;
import cn.zjnktion.billy.task.WriteTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * Created by zjnktion on 2016/4/2.
 */
public abstract class NioProcessorTemplate<C extends ContextTemplete> implements Processor<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioProcessorTemplate.class);

    protected final Executor executor;

    private final Queue<C> addQueue = new ConcurrentLinkedQueue<C>();

    private final Queue<C> removeQueue = new ConcurrentLinkedQueue<C>();

    private final Queue<C> flushQueue = new ConcurrentLinkedQueue<C>();

    private final Queue<C> updateSuspendStatusQueue = new ConcurrentLinkedQueue<C>();

    private volatile boolean disposing;
    private volatile boolean disposed;
    private final Object disposalLock = new Object();
    private final GenericFuture disposalFuture = new GenericFuture(null);

    protected NioProcessorTemplate(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Cannot set a null executor to NioProcessorTemplate.");
        }
        this.executor = executor;
    }

    public final void add(C context) {
        if (disposing || disposed) {
            throw new IllegalStateException("Processor has already disposed.");
        }

        addQueue.add(context);

        work();
    }

    public final void remove(C context) {
        if (!removeQueue.contains(context)) {
            removeQueue.add(context);
        }

        work();
    }

    public final void write(C context, WriteTask writeTask) {
        WriteTaskQueue writeTaskQueue = context.getWriteTaskQueue();
        writeTaskQueue.offer(writeTask);

        if (!context.isWriteSuspended()) {
            this.flush(context);
        }
    }

    public final void flush(C context) {
        if (context.setReadyForFlush(true)) {
            flushQueue.add(context);
            wakeupSelector();
        }
    }

    public final void updateSuspendStatus(C context) {
        try {
            setInterestedInRead(context, !context.isReadSuspended());
        } catch (Exception e) {
            FilterChain filterChain = context.getFilterChain();
            filterChain.fireExceptionCaught();
        }

        try {
            setInterestedInWrite(context, (!context.getWriteTaskQueue().isEmpty() && !context.isWriteSuspended()));
        } catch (Exception e) {
            FilterChain filterChain = context.getFilterChain();
            filterChain.fireExceptionCaught();
        }
    }

    public final void dispose() {
        if (disposing || disposed) {
            return;
        }

        synchronized (disposalLock) {
            disposing = true;
            work();
        }

        disposalFuture.awaitUninterruptibly();

        disposed = true;
    }

    /**
     * 面向连接和无连接有不同的实现
     */
    protected abstract void work();

    /**
     * 面向连接和无连接有不同的实现
     */
    protected abstract void wakeupSelector();

    /**
     * 面向连接和无连接有不同的实现
     * @param context
     * @param isInterested
     * @throws Exception
     */
    protected abstract void setInterestedInRead(C context, boolean isInterested) throws Exception;

    /**
     * 面向连接和无连接有不同的实现
     * @param context
     * @param isInterested
     * @throws Exception
     */
    protected abstract void setInterestedInWrite(C context, boolean isInterested) throws Exception;

}
