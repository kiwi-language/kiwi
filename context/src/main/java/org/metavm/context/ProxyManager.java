package org.metavm.context;

import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

public class ProxyManager {

    public static final String PROXY_SUFFIX = "__Proxy__";
    final Map<TypeElement, String> proxyNames = new HashMap<>();

    public String getProxyName(TypeElement clazz) {
        var proxyName = proxyNames.get(clazz);
        if (proxyName != null)
            return proxyName;
        return clazz.getQualifiedName().toString();
    }

    boolean isProxied(TypeElement clazz) {
        return proxyNames.containsKey(clazz);
    }

    public boolean addProxied(TypeElement clazz) {
        return proxyNames.put(clazz, clazz.getSimpleName() + PROXY_SUFFIX) == null;
    }

}
