package cn.zjnktion.billy.future;

/**
 * Created by zhengjn on 2016/4/1.
 */
public class EngineOperationFuture extends GenericFuture {

    public EngineOperationFuture() {
        super(null);
    }

    public final boolean isDone() {
        return getResult() == Boolean.TRUE;
    }

    public final void setDone() {
        setResult(Boolean.TRUE);
    }

    public final Exception getCause() {
        if (getResult() instanceof Exception) {
            return (Exception) getResult();
        }

        return null;
    }

    public final void setCause(Exception exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception");
        }

        setResult(exception);
    }
}
