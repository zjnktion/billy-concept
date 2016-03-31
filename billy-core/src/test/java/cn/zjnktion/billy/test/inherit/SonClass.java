package cn.zjnktion.billy.test.inherit;

/**
 * Created by zjnktion on 2016/3/31.
 */
public class SonClass extends ParentClass {

    public SonClass(String str) {
        super(str);
    }

    @Override
    protected void init() {
        System.out.println(str);
    }

    public static void main(String[] args) {
        new SonClass("a");
    }
}
