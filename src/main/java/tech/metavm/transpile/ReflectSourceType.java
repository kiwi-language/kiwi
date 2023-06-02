package tech.metavm.transpile;


import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Type;
import java.util.Objects;

public class ReflectSourceType implements SourceType {

    protected final Type type;

    public ReflectSourceType(Type type) {
        this.type = type;
    }

    @Override
    public boolean isAssignableFrom(SourceType that) {
        return false;
    }

    @Override
    public Class<?> getReflectClass() {
        return ReflectUtils.eraseToClass(type);
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReflectSourceType) obj;
        return Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "ReflectSourceType[" +
                "type=" + type + ']';
    }

}
