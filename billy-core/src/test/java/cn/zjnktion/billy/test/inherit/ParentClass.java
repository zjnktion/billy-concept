package cn.zjnktion.billy.test.inherit;

/**
 * Created by zjnktion on 2016/3/31.
 */
public abstract class ParentClass {

    protected final String str;

    protected ParentClass(String str) {
        this.str = str;
        init();
    }

    protected abstract void init();

}
