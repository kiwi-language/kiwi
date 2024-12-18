package org.metavm.entity;

import org.metavm.api.Index;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.type.Field;

public enum StdField implements ValueHolderOwner<Field> {

    enumName(Enum.class, "name"),
    enumOrdinal(Enum.class, "ordinal"),
    indexMapName(Index.class, "name"),
    httpRequestImplMethod(HttpRequestImpl.class, "method"),
    httpRequestImplRequestURI(HttpRequestImpl.class, "requestURI"),
    httpRequestImplCookies(HttpRequestImpl.class, "cookies"),
    httpRequestImplHeaders(HttpRequestImpl.class, "headers"),
    httpResponseImplCookies(HttpResponseImpl.class, "cookies"),
    httpResponseImplHeaders(HttpResponseImpl.class, "headers"),
    httpCookieName(HttpCookie.class, "name"),
    httpCookieValue(HttpCookie.class, "value"),
    httpHeaderName(HttpHeader.class, "name"),
    httpHeaderValue(HttpHeader.class, "value"),
    ;

    private final Class<?> javaClass;
    private final String fieldName;
    private ValueHolder<Field> fieldHolder = new HybridValueHolder<>();

    StdField(Class<?> javaClass, String fieldName) {
        this.javaClass = javaClass;
        this.fieldName = fieldName;
    }

    public Field get() {
        return fieldHolder.get();
    }

    private void set(Field field) {
        fieldHolder.set(field);
    }

    public void setLocal(Field field) {
        fieldHolder.setLocal(field);
    }

    public void init(DefContext defContext, boolean local) {
        var klass = defContext.getKlass(javaClass);
        var field = klass.getFieldByName(fieldName);
        if(local)
            setLocal(field);
        else
            set(field);
    }

    public void setValueHolder(ValueHolder<Field> methodHolder) {
        this.fieldHolder = methodHolder;
    }

    @Override
    public ValueHolder<Field> getValueHolder() {
        return fieldHolder;
    }

    public static void initialize(DefContext defContext, boolean local) {
        for (StdField value : values()) {
            value.init(defContext, local);
        }
    }

}
