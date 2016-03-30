package cn.zjnktion.billy.test.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengjn on 2016/3/30.
 */
public class UnmodifiedMap {

    public static void main(String[] args) {
        Map<Long, String> source = new HashMap<Long, String>();
        Map<Long, String> copy = Collections.unmodifiableMap(source);

        source.put(1L, "1");
        copy.put(2L, "2");

        System.out.println("a");
    }

}
