package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

@Slf4j
public class HttpResponseImplNative extends NativeBase {

    private final HttpResponseImpl instance;

    public HttpResponseImplNative(ClassInstance instance) {
        this.instance = (HttpResponseImpl) instance;
    }

    public Value addCookie(Value name, Value value, CallContext callContext) {
        instance.addCookie(name.stringValue(), value.stringValue());
        return Instances.nullInstance();
    }

    public Value addHeader(Value name, Value value, CallContext callContext) {
        instance.addHeader(name.stringValue(), value.stringValue());
        return Instances.nullInstance();
    }

    public Value getCookies(CallContext callContext) {
        return Instances.list(StdKlass.httpCookie.type(),
                Utils.map(instance.getCookies(), c -> (Value) c));
    }

    public Value getHeaders(CallContext callContext) {
        return Instances.list(StdKlass.httpHeader.type(),
                Utils.map(instance.getHeaders(), h -> (Value) h));
    }

}
