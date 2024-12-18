package org.metavm.entity.natives;

import org.metavm.entity.StdField;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

public class HttpRequestImplNative extends NativeBase {

    private final ClassInstance instance;

    public HttpRequestImplNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value getMethod(CallContext callContext) {
        return instance.getField(StdField.httpRequestImplMethod.get());
    }

    public Value getRequestURI(CallContext callContext) {
        return instance.getField(StdField.httpRequestImplRequestURI.get());
    }

    public Value getCookie(Value name, CallContext callContext) {
        var n = name.stringValue();
        var cookies = instance.getField(StdField.httpRequestImplCookies.get()).resolveArray();
        for (Value c : cookies) {
            var cookie = c.resolveObject();
            if (n.equals(cookie.getField(StdField.httpCookieName.get()).stringValue()))
                return cookie.getField(StdField.httpCookieValue.get());
        }
        return Instances.nullInstance();
    }

    public Value getHeader(Value name, CallContext callContext) {
        var n = name.stringValue();
        var headers = instance.getField(StdField.httpRequestImplHeaders.get()).resolveArray();
        for (Value h : headers) {
            var header = h.resolveObject();
            if (n.equals(header.getField(StdField.httpHeaderName.get()).stringValue()))
                return header.getField(StdField.httpHeaderValue.get());
        }
        return Instances.nullInstance();
    }

}
