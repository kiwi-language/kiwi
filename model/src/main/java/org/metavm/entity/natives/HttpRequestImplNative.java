package org.metavm.entity.natives;

import org.metavm.http.HttpRequestImpl;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

public class HttpRequestImplNative implements NativeBase {

    private final HttpRequestImpl instance;

    public HttpRequestImplNative(ClassInstance instance) {
        this.instance = (HttpRequestImpl) instance;
    }

    public Value getMethod(CallContext callContext) {
        return Instances.stringInstance(instance.method);
    }

    public Value getRequestURI(CallContext callContext) {
        return Instances.stringInstance(instance.requestURI);
    }

    public Value getCookie(Value name, CallContext callContext) {
        var n = name.stringValue();
        var c = instance.getCookie(n);
        return c != null ? Instances.stringInstance(c) : Instances.nullInstance();
    }

    public Value getHeader(Value name, CallContext callContext) {
        var n = name.stringValue();
        var h = instance.getHeader(n);
        return h != null ? Instances.stringInstance(h) : Instances.nullInstance();
    }

}
