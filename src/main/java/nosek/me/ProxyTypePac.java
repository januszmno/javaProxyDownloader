package nosek.me;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ProxyTypePac {

    DIRECT("DIRECT", ""),
    PROXY("PROXY", ""),
    SOCKS("SOCKS", "socks"),
    HTTP("HTTP", "http"),
    HTTPS("HTTPS", "https"),
    SOCKS4("SOCKS4", "socks"),
    SOCKS5("SOCKS5", "socks");


    private String name;
    private String protocol;

    private static final Map<String,ProxyTypePac> ENUM_NAME_MAP;
    private static final Map<String,ProxyTypePac> ENUM_PROTO_MAP;

    ProxyTypePac (String name, String protocol) {
        this.name = name;
        this.protocol = protocol;
    }

    public String getName() {
        return this.name;
    }

    public String getProtocol() {
        return this.protocol;
    }

    static {
        Map<String,ProxyTypePac> nameMap = new ConcurrentHashMap<String, ProxyTypePac>();
        Map<String,ProxyTypePac> protocolMap = new ConcurrentHashMap<String, ProxyTypePac>();
        for (ProxyTypePac instance : ProxyTypePac.values()) {
            nameMap.put(instance.getName().toLowerCase(),instance);
            protocolMap.put(instance.getProtocol().toLowerCase(),instance);
        }
        ENUM_NAME_MAP = Collections.unmodifiableMap(nameMap);
        ENUM_PROTO_MAP = Collections.unmodifiableMap(protocolMap);
    }

    public static ProxyTypePac getName(String name) {
        return ENUM_NAME_MAP.get(name.toLowerCase());
    }


}
