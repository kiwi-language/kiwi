package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import java.util.Map;

@Slf4j
public class HttpResponseImplNative extends NativeBase {

    private final ClassInstance instance;

    public HttpResponseImplNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value addCookie(Value name, Value value, CallContext callContext) {
        var n = name.stringValue();
        var cookies = instance.getField(StdField.httpResponseImplCookies.get()).resolveArray();
        cookies.removeIf(e -> e.resolveObject().getField(StdField.httpCookieName.get()).stringValue().equals(n));
        cookies.addElement(ClassInstance.create(
                Map.of(StdField.httpCookieName.get(), name, StdField.httpCookieValue.get(), value),
                StdKlass.httpCookie.type()
        ).getReference());
        return Instances.nullInstance();
    }

    public Value addHeader(Value name, Value value, CallContext callContext) {
        var n = name.stringValue();
        var headers = instance.getField(StdField.httpResponseImplHeaders.get()).resolveArray();
        headers.removeIf(e -> e.resolveObject().getField(StdField.httpHeaderName.get()).stringValue().equals(n));
        headers.addElement(ClassInstance.create(
                Map.of(StdField.httpHeaderName.get(), name, StdField.httpHeaderValue.get(), value),
                StdKlass.httpHeader.type()
        ).getReference());
        return Instances.nullInstance();
    }

    public Value getCookies(CallContext callContext) {
        var cookies = instance.getField(StdField.httpResponseImplCookies.get()).resolveArray();
        return Instances.list(StdKlass.httpCookie.type(), cookies);
    }

    public Value getHeaders(CallContext callContext) {
        var headers = instance.getField(StdField.httpResponseImplHeaders.get()).resolveArray();
        return Instances.list(StdKlass.httpHeader.type(), headers);
    }

}
