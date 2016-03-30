package cn.zjnktion.billy.service;

import cn.zjnktion.billy.context.ContextConfig;

import java.net.SocketAddress;

/**
 * {@link Engine} 元信息
 * Created by zhengjn on 2016/3/30.
 */
public interface EngineMetainfo {

    String getName();

    String getTransportType();

    Class<? extends SocketAddress> getSocketAddressType();

    Class<? extends ContextConfig> getContextConfigType();

}
