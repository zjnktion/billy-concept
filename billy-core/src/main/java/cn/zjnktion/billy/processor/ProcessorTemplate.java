package cn.zjnktion.billy.processor;

import cn.zjnktion.billy.context.ContextStatus;
import cn.zjnktion.billy.context.ContextTemplete;
import cn.zjnktion.billy.filter.FilterChain;
import cn.zjnktion.billy.filter.FilterChainBuilder;
import cn.zjnktion.billy.future.GenericFuture;
import cn.zjnktion.billy.service.EngineTemplate;
import cn.zjnktion.billy.task.WriteTask;
import cn.zjnktion.billy.task.WriteTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zjnktion on 2016/4/2.
 */
public abstract class ProcessorTemplate<C extends ContextTemplete> implements Processor<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorTemplate.class);

    private final Executor executor;

    private final Queue<C> addQueue = new ConcurrentLinkedQueue<C>();

    private final Queue<C> removeQueue = new ConcurrentLinkedQueue<C>();

    private final Queue<C> flushQueue = new ConcurrentLinkedQueue<C>();

    private final Queue<C> updateSuspendStatusQueue = new ConcurrentLinkedQueue<C>();

    private volatile boolean disposing;
    private volatile boolean disposed;
    private final Object disposalLock = new Object();
    private final GenericFuture disposalFuture = new GenericFuture(null);

    private AtomicReference<Worker> workerRef = new AtomicReference<Worker>();
    private long lastIdleCheckTime;
    private static final long SELECT_TIMEOUT = 1000L;
    protected AtomicBoolean wakeupCalled = new AtomicBoolean(false);

    protected ProcessorTemplate(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Cannot set a null executor to processor.");
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
        }
        catch (Exception e) {
            FilterChain filterChain = context.getFilterChain();
            filterChain.fireExceptionCaught();
        }

        try {
            setInterestedInWrite(context, (!context.getWriteTaskQueue().isEmpty() && !context.isWriteSuspended()));
        }
        catch (Exception e) {
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

    public final boolean isDisposing() {
        return disposing;
    }

    public final boolean isDisposed() {
        return disposed;
    }

    protected abstract void work();

    protected abstract void wakeupSelector();

    protected abstract void setInterestedInRead(C context, boolean isInterested) throws Exception;

    protected abstract void setInterestedInWrite(C context, boolean isInterested) throws Exception;

    private class Worker implements Runnable {

        public void run() {
            assert (workerRef.get() == this);

            int contextCount = 0;
            lastIdleCheckTime = System.currentTimeMillis();

            while(true) {
                try {
                    long selectStartTime = System.currentTimeMillis();
                    int selected = select(SELECT_TIMEOUT);
                    long selectEndTime = System.currentTimeMillis();
                    long dealTime = selectEndTime - selectStartTime;

                    if (!wakeupCalled.getAndSet(false) && selected == 0 && dealTime < 100L) {
                        if (isBroken()) {
                            LOGGER.warn("Connection has been broken.");
                        }
                        else {
                            LOGGER.warn("Create a new select because selected is 0 in deal time = {} millis", dealTime);

                            // 通常这个操作是由于java原生nio的一个很严苛的竟态条件才会产生的。
                            // 一旦触发了这个竟态条件，cpu占用率将会出现不可预料的100%。
                            // 为了解决这个竟态条件，找了很多攻略，一般都是采用新建selector并且关闭旧的selector的方法。
                            newSelector();
                        }
                    }

                    contextCount += addNewContexts();

                    updateSuspendMask();

                    if (selected > 0) {
                        LOGGER.debug("Processing...");
                        process();
                    }

                    long currentTime = System.currentTimeMillis();
                    flush(currentTime);

                    contextCount -= removeContexts();
                }
            }
        }
    }

    protected abstract int select() throws Exception;

    protected abstract int select(long timeout) throws Exception;

    protected abstract void init(C context) throws Exception;

    protected abstract void destroy(C context) throws Exception;

    protected abstract boolean isBroken() throws IOException;

    protected abstract void newSelector() throws IOException;

    protected abstract ContextStatus getContextStatus(C context);
    
    protected abstract Iterator<C> selectedContexts();
    
    protected abstract boolean isReadable(C context);

    protected abstract boolean isWritable(C context);

    private int addNewContexts() {
        int addedContexts = 0;

        for (C context = addQueue.poll(); context != null; context = addQueue.poll()) {
            if (addContext(context)) {
                addedContexts++;
            }
        }

        return addedContexts;
    }

    private boolean addContext(C context) {
        boolean registered = false;

        try {
            init(context);
            registered = true;

            FilterChainBuilder chainBuilder = context.getEngine().getFilterChain();
            chainBuilder.buildFilterChain(context.getFilterChain());
            ((EngineTemplate) context.getEngine()).fireContextCreated(context);
        }
        catch (Exception e) {
            // TODO we should monitor this exception and deliver to business handler.

            try {
                destroy(context);
            }
            catch (Exception e1) {
                // we should monitor this exception and deliver to business handler.
            }
            finally {
                registered = false;
            }
        }

        return registered;
    }

    private void updateSuspendMask() {
        int queueSize = updateSuspendStatusQueue.size();

        while (queueSize > 0) {
            C context = updateSuspendStatusQueue.poll();

            if (context == null) {
                return;
            }

            ContextStatus status = getContextStatus(context);

            switch (status) {
                case OPENING: {
                    updateSuspendStatusQueue.add(context);
                    break;
                }
                case OPENED: {
                    updateSuspendStatus(context);
                    break;
                }
                case CLOSING: {
                    break;
                }
                default: {
                    throw new IllegalStateException("Context invalid status " + String.valueOf(status) + ".");
                }
            }

            queueSize--;
        }
    }

    private void process() throws Exception {
        for (Iterator<C> itr = selectedContexts(); itr.hasNext();) {
            C context = itr.next();
            
            if (isReadable(context) && !context.isReadSuspended()) {
                read(context);
            }

            if (isWritable(context) && !context.isWriteSuspended()) {
                if (context.setReadyForFlush(true)) {
                    flushQueue.add(context);
                }
            }

            itr.remove();
        }
    }
    
    private void read(C context) {
        // TODO: 有待实现
    }

    private void flush(long currentTime) {
        if (flushQueue.isEmpty()) {
            return;
        }

        do {
            C context = flushQueue.poll();

            if (context == null) {
                break;
            }

            context.unreadyForFlush();

            ContextStatus status = getContextStatus(context);

            switch (status) {
                case OPENING: {
                    readyFlush(context);
                    return;
                }
                case OPENED: {
                    try {
                        boolean flushedAll = flushNow(context, 1000L);
                        // TODO 代码补全
                    break;
                }
                case CLOSING: {
                    break;
                }
            }
        }
    }

    private void readyFlush(C context) {
        if (context.setReadyForFlush(true)) {
            flushQueue.add(context);
        }
    }

    private boolean flushNow(C context, long timeout) {
        if (!context.isConnected()) {
            readyRemove(context);
            return false;
        }

        final boolean canFragmentation = context.getEngine().getTransportMetadata().canFragmentation();

        final WriteTaskQueue writeTaskQueue = context.getWriteTaskQueue();

        // 网上的一个拆包读写平衡的一个经验值
        final int maxWrittenBytes = context.getEngine().getContextConfig().getMaxReadBufferSize() + (context.getEngine().getContextConfig().getMaxReadBufferSize() >>> 1);
        int writtenBytes = 0;
        WriteTask task = null;

        try {
            // 清除已经注册的OP_WRITE
            setInterestedInWrite(context, false);

            do {
                task = context.getCurrentWriteTask();

                if (task == null) {
                    task = writeTaskQueue.poll();

                    if (task == null) {
                        break;
                    }

                    context.setCurrentWriteTask(task);
                }

                int localWrittenBytes = 0;
                Object message = task.getMessage();

                // TODO 根据自定义ByteBuffer类型来写数据
                // 代码补全

                if (localWrittenBytes == 0) {
                    // 系统缓冲区已经满了，可以flush了
                    setInterestedInWrite(context, true);
                }

                writtenBytes += localWrittenBytes;

                if (writtenBytes > maxWrittenBytes) {
                    // 要写的数据太大
                    readyFlush(context);
                    return false;
                }

                // TODO 根据自定义的ByteBuffer类型来释放已经写的数据
                // 代码补全

            } while (writtenBytes < maxWrittenBytes);
        }
        catch (Exception e) {
            if (task != null) {
                task.getFuture().setCause(e);
            }

            FilterChain filterChain = context.getFilterChain();
            filterChain.fireExceptionCaught();
            return false;
        }

        return true;
    }

    private void readyRemove(C context) {
        if (!removeQueue.contains(context)) {
            removeQueue.add(context);
        }
    }

}
