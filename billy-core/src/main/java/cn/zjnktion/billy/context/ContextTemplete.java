package cn.zjnktion.billy.context;

import cn.zjnktion.billy.future.DefaultReadFuture;
import cn.zjnktion.billy.future.ReadFuture;
import cn.zjnktion.billy.service.Engine;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
