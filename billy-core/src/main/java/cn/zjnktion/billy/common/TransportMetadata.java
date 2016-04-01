package cn.zjnktion.billy.common;

import cn.zjnktion.billy.context.ContextConfig;
import cn.zjnktion.billy.service.Engine;

import java.net.SocketAddress;

/**
 * {@link Engine} 元信息
 * Created by zhengjn on 2016/3/30.
 */
public interface TransportMetadata {

    enum PROVIDER_NAMES {
        BILLY("billy"),
        NIO("nio");

        private String value;

        private PROVIDER_NAMES(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    enum TYPE_NAME {
        SOCKET("socket"),
        DATAGRAM("datagram");

        private String value;

        private TYPE_NAME(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    String getProviderName();

    String getTypeName();

    boolean isConnectionless();

    Class<? extends SocketAddress> getSocketAddressType();

    Class<? extends ContextConfig> getContextConfigType();

}
