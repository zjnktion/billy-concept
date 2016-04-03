package cn.zjnktion.billy.context;

import cn.zjnktion.billy.common.IdleType;
import cn.zjnktion.billy.filter.FilterChain;
import cn.zjnktion.billy.future.*;
import cn.zjnktion.billy.processor.Processor;
import cn.zjnktion.billy.service.Engine;
import cn.zjnktion.billy.task.DefaultWriteTask;
import cn.zjnktion.billy.task.WriteTask;
import cn.zjnktion.billy.task.WriteTaskQueue;
import cn.zjnktion.billy.task.exception.WriteClosedContextException;
import cn.zjnktion.billy.task.exception.WriteException;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhengjn on 2016/3/31.
 */
public abstract class ContextTemplete implements Context {

    private long id;
    private static AtomicLong idGenerator = new AtomicLong(0);

    private final Engine engine;

    private final AtomicBoolean readyForFlush = new AtomicBoolean();

    private long lastReadTime;
    private long lastWriteTime;
    private long lastIdleTimeForBoth;
    private long lastIdleTimeForRead;
    private long lastIdleTimeForWrite;

    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    private static final String READY_READ_QUEUE = "readyReadQueue";
    private static final String WAITING_READ_QUEUE = "waitingReadQueue";
    private boolean readSuspended = false;

    private boolean writeSuspended = false;
    private WriteTaskQueue writeTaskQueue;
    private WriteTask currentWriteTask;
    private static final WriteTask CLOSE_TASK = new DefaultWriteTask(new Object());

    private final Object closeLock = new Object();
    private final CloseFuture closeFuture = new DefaultCloseFuture(this);
    private volatile boolean closing;

    private AtomicInteger idleCountForBoth = new AtomicInteger();
    private AtomicInteger idleCountForRead = new AtomicInteger();
    private AtomicInteger idleCountForWrite = new AtomicInteger();

    protected ContextTemplete(Engine engine) {
        this.engine = engine;

        long currentTime = System.currentTimeMillis();
        lastReadTime = currentTime;
        lastWriteTime = currentTime;
        lastIdleTimeForBoth = currentTime;
        lastIdleTimeForRead = currentTime;
        lastIdleTimeForWrite = currentTime;

        id = idGenerator.incrementAndGet();
    }

    public final long getId() {
        return id;
    }

    public final Engine getEngine() {
        return engine;
    }

    public final ReadFuture read() {
        if (!engine.getContextConfig().isAsyncReadEnable()) {
            throw new IllegalStateException("Cannot async read while config disable.");
        }

        Queue<ReadFuture> readyReadQueue = getReadyReadQueue();
        ReadFuture readFuture;

        synchronized (readyReadQueue) {
            readFuture = readyReadQueue.poll();
            if (readFuture != null) {
                if (readFuture.isClosed()) {
                    readyReadQueue.offer(readFuture);
                }
            } else {
                readFuture = new DefaultReadFuture(this);
                getWaitingReadQueue().offer(readFuture);
            }
        }

        return readFuture;
    }

    public final void suspendRead() {
        readSuspended = true;
        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateSuspendStatus(this);
    }

    public final void resumeRead() {
        readSuspended = false;
        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateSuspendStatus(this);
    }

    public final boolean isReadSuspended() {
        return readSuspended;
    }

    public final WriteFuture write(Object message) {
        return write(message, null);
    }

    public final WriteFuture write(Object message, SocketAddress destination) {
        if (message == null) {
            throw new IllegalArgumentException("Cannot send a null message.");
        }

        if (!getEngine().getTransportMetadata().isConnectionless() && (destination != null)) {
            throw new UnsupportedOperationException("We can't send a message to a connection-oriented context with special remote address.");
        }

        if (isClosing() || !isConnected()) {
            DefaultWriteFuture future = new DefaultWriteFuture(this);
            WriteTask task = new DefaultWriteTask(message, future, destination);
            WriteException writeException = new WriteClosedContextException(task);
            future.setCause(writeException);
            return future;
        }

        WriteFuture writeFuture = new DefaultWriteFuture(this);
        WriteTask writeTask = new DefaultWriteTask(message, writeFuture, destination);

        FilterChain filterChain = getFilterChain();
        filterChain.fireFilterWrite();

        return writeFuture;
    }

    public final void suspendWrite() {
        writeSuspended = true;

        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateSuspendStatus(this);
    }

    public void resumeWrite() {
        writeSuspended = false;

        if (isClosing() || !isConnected()) {
            return;
        }
        getProcessor().updateSuspendStatus(this);
    }

    public final boolean isWriteSuspended() {
        return writeSuspended;
    }

    public final WriteTaskQueue getWriteTaskQueue() {
        if (writeTaskQueue == null) {
            throw new IllegalStateException("Write task queue is null.");
        }

        return writeTaskQueue;
    }

    public final WriteTask getCurrentWriteTask() {
        return currentWriteTask;
    }

    public final void setCurrentWriteTask(WriteTask currentWriteTask) {
        this.currentWriteTask = currentWriteTask;
    }

    public final Object getCurrentWriteMessage() {
        WriteTask currentWriteTask = getCurrentWriteTask();

        return currentWriteTask == null ? null : currentWriteTask.getMessage();
    }

    public final CloseFuture closeImmediately() {
        synchronized (closeLock) {
            if (isClosing()) {
                return closeFuture;
            }

            closing = true;
        }

        FilterChain filterChain = getFilterChain();
        filterChain.fireFilterClose();

        return closeFuture;
    }

    public final CloseFuture closeOnFlush() {
        if (!isClosing()) {
            getWriteTaskQueue().offer(CLOSE_TASK);
            getProcessor().flush(this);
        }

        return closeFuture;
    }

    public final CloseFuture getCloseFuture() {
        return closeFuture;
    }

    public final boolean isConnected() {
        return !closeFuture.isClosed();
    }

    public final long getLastIoTime() {
        return Math.max(lastReadTime, lastWriteTime);
    }

    public final long getLastReadTime() {
        return lastReadTime;
    }

    public final long getLastWriteTime() {
        return lastReadTime;
    }

    public final boolean isIdle(IdleType idleType) {
        if (idleType == IdleType.BOTH_IDLE) {
            return idleCountForBoth.get() > 0;
        }

        if (idleType == IdleType.READ_IDLE) {
            return idleCountForRead.get() > 0;
        }

        if (idleType == IdleType.WRITE_IDLE) {
            return idleCountForWrite.get() > 0;
        }

        throw new IllegalArgumentException("Unknown idle type.");
    }

    public final long getLastIdleTime(IdleType idleType) {
        if (idleType == IdleType.BOTH_IDLE) {
            return lastIdleTimeForBoth;
        }

        if (idleType == IdleType.READ_IDLE) {
            return lastIdleTimeForRead;
        }

        if (idleType == IdleType.WRITE_IDLE) {
            return lastIdleTimeForWrite;
        }

        throw new IllegalArgumentException("Unknown idle type.");
    }

    public final int getIdleCount(IdleType idleType) {
        if (getEngine().getContextConfig().getIdleTime(idleType) == 0) {
            if (idleType == IdleType.BOTH_IDLE) {
                idleCountForBoth.set(0);
            }

            if (idleType == IdleType.READ_IDLE) {
                idleCountForRead.set(0);
            }

            if (idleType == IdleType.WRITE_IDLE) {
                idleCountForWrite.set(0);
            }
        }

        if (idleType == IdleType.BOTH_IDLE) {
            return idleCountForBoth.get();
        }

        if (idleType == IdleType.READ_IDLE) {
            return idleCountForRead.get();
        }

        if (idleType == IdleType.WRITE_IDLE) {
            return idleCountForWrite.get();
        }

        throw new IllegalArgumentException("Unknown idle type.");
    }

    public final boolean isClosing() {
        return closing || closeFuture.isClosed();
    }

    public final boolean isReadyForFlush() {
        return readyForFlush.get();
    }

    public final void readyForFlush() {
        readyForFlush.set(true);
    }

    public final void unreadyForFlush() {
        readyForFlush.set(false);
    }

    public final boolean setReadyForFlush(boolean ready) {
        if (ready) {
            return readyForFlush.compareAndSet(false, true);
        }

        readyForFlush.set(false);
        return true;
    }

    public final Object getAttr(String key) {
        return attributes.get(key);
    }

    public final Object setAttr(String key, Object value) {
        return attributes.put(key, value);
    }

    public final Object setAttrIfAbsent(String key, Object value) {
        return attributes.putIfAbsent(key, value);
    }

    public final void removeAttr(String key) {
        attributes.remove(key);
    }

    public final boolean containsAttr(String key) {
        return attributes.containsKey(key);
    }

    public final void increaseIdleCount(IdleType idleType, long currentTime) {
        if (idleType == IdleType.BOTH_IDLE) {
            idleCountForBoth.incrementAndGet();
            lastIdleTimeForBoth = currentTime;
            return;
        }

        if (idleType == IdleType.READ_IDLE) {
            idleCountForRead.incrementAndGet();
            lastIdleTimeForRead = currentTime;
            return;
        }

        if (idleType == IdleType.WRITE_IDLE) {
            idleCountForWrite.incrementAndGet();
            lastIdleTimeForWrite = currentTime;
            return;
        }

        throw new IllegalArgumentException("Unknown idle type.");
    }

    protected abstract Processor getProcessor();

    private Queue<ReadFuture> getReadyReadQueue() {
        Queue<ReadFuture> readyReadQueue = (Queue<ReadFuture>) getAttr(READY_READ_QUEUE);

        if (readyReadQueue == null) {
            readyReadQueue = new ConcurrentLinkedQueue<ReadFuture>();
            Queue<ReadFuture> oldReadyReadQueue = (Queue<ReadFuture>) setAttrIfAbsent(READY_READ_QUEUE, readyReadQueue);

            if (oldReadyReadQueue != null) {
                readyReadQueue = oldReadyReadQueue;
            }
        }

        return readyReadQueue;
    }

    private Queue<ReadFuture> getWaitingReadQueue() {
        Queue<ReadFuture> waitingReadQueue = (Queue<ReadFuture>) getAttr(WAITING_READ_QUEUE);

        if (waitingReadQueue == null) {
            waitingReadQueue = new ConcurrentLinkedQueue<ReadFuture>();
            Queue<ReadFuture> oldWaitingReadQueue = (Queue<ReadFuture>) setAttrIfAbsent(WAITING_READ_QUEUE, waitingReadQueue);

            if (oldWaitingReadQueue != null) {
                waitingReadQueue = oldWaitingReadQueue;
            }
        }

        return waitingReadQueue;
    }
}
