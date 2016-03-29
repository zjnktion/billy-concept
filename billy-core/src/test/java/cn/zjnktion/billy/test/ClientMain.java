package cn.zjnktion.billy.test;

import java.io.IOException;

/**
 * Created by zjnktion on 2016/3/12.
 */
public class ClientMain {

    public static void main(String[] args) throws IOException {
        SampleNioClient client = SampleNioClient.open();
        client.connect(9090);
    }

}