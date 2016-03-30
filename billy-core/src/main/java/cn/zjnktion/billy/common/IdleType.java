package cn.zjnktion.billy.common;

/**
 * 超时类型
 * Created by zhengjn on 2016/3/30.
 */
public class IdleType {

    public static final String READ_IDLE_NAME = "read idle";
    public static final String WRITE_IDLE_NAME = "write idle";
    public static final String BOTH_IDLE_NAME = "both idle";

    private String typeName;

    private IdleType(String typeName) {
        this.typeName = typeName;
    }

    public static final IdleType READ_IDLE = new IdleType(READ_IDLE_NAME);
    public static final IdleType WRITE_TYPE = new IdleType(WRITE_IDLE_NAME);
    public static final IdleType BOTH_IDLE = new IdleType(BOTH_IDLE_NAME);

}
