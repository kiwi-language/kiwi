package tech.metavm.transpile;

import tech.metavm.transpile.ir.IRType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LambdaType extends IRType implements SourceType {
    private final List<Type> parameterTypes;
    private final boolean hasReturnType;

    public LambdaType(List<Type> parameterTypes, boolean hasReturnType) {
        super("Lambda");
        this.parameterTypes = parameterTypes;
        this.hasReturnType = hasReturnType;
    }

    @Override
    public boolean isAssignableFrom(SourceType that) {
        return false;
    }

    @Override
    public Class<?> getReflectClass() {
        throw new UnsupportedOperationException();
    }

    public List<Type> parameterTypes() {
        return parameterTypes;
    }

    public boolean hasReturnType() {
        return hasReturnType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LambdaType) obj;
        return Objects.equals(this.parameterTypes, that.parameterTypes) &&
                this.hasReturnType == that.hasReturnType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, hasReturnType);
    }

    @Override
    public String toString() {
        return "LambdaType[" +
                "parameterTypes=" + parameterTypes + ", " +
                "hasReturnType=" + hasReturnType + ']';
    }


    @Override
    public boolean isAssignableFrom0(IRType that) {
        return false;
    }

    @Override
    public List<IRType> getReferences() {
        return null;
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return null;
    }
}
