package cn.zjnktion.billy.common;

import cn.zjnktion.billy.context.ContextConfig;

import java.net.SocketAddress;

/**
 * Created by zjnktion on 2016/4/4.
 */
public class DefaultTransportMetadata implements TransportMetadata {

    private final String providerName;

    private final String typeName;

    private final boolean connectionless;

    private final boolean fragmentationable;

    private final Class<? extends SocketAddress> socketAddressType;

    private final Class<? extends ContextConfig> contextConfigType;

    public DefaultTransportMetadata(String providerName, String typeName, boolean connectionless, boolean fragmentationable, Class<? extends SocketAddress> socketAddressType, Class<? extends ContextConfig> contextConfigType) {

        if (providerName == null) {
            throw new IllegalArgumentException("Cannot set a null provider name.");
        }
        if (typeName == null) {
            throw new IllegalArgumentException("Cannot set a null type name.");
        }

        providerName = providerName.trim().toLowerCase();
        if (providerName.length() == 0) {
            throw new IllegalArgumentException("Provider name is empty.");
        }

        typeName = typeName.trim().toLowerCase();
        if (typeName.length() == 0) {
            throw new IllegalArgumentException("Type name is empty.");
        }

        if (socketAddressType == null) {
            throw new IllegalArgumentException("Cannot set a null socket address type");
        }

        if (contextConfigType == null) {
            throw new IllegalArgumentException("Cannot set a null context config type");
        }

        this.providerName = providerName;
        this.typeName = typeName;
        this.connectionless = connectionless;
        this.fragmentationable = fragmentationable;
        this.socketAddressType = socketAddressType;
        this.contextConfigType = contextConfigType;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isConnectionless() {
        return connectionless;
    }

    public boolean canFragmentation() {
        return fragmentationable;
    }

    public Class<? extends SocketAddress> getSocketAddressType() {
        return socketAddressType;
    }

    public Class<? extends ContextConfig> getContextConfigType() {
        return contextConfigType;
    }
}
