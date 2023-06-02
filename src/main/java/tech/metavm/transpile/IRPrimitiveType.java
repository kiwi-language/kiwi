package tech.metavm.transpile;

import tech.metavm.transpile.ir.IRPrimitiveKind;
import tech.metavm.transpile.ir.IRType;

import java.util.List;
import java.util.Map;

public class IRPrimitiveType extends IRType {

    private final IRPrimitiveKind kind;

    public IRPrimitiveType(IRPrimitiveKind kind) {
        super(kind.getName());
        this.kind = kind;
    }

    public IRPrimitiveKind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IRPrimitiveType that) {
            return kind == that.kind;
        }
        return false;
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        return this == that;
    }

    @Override
    public List<IRType> getReferences() {
        return List.of();
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return this;
    }

    public boolean isConvertibleFrom(IRPrimitiveType that) {
        if(kind == IRPrimitiveKind.DOUBLE) {
            return that.kind == IRPrimitiveKind.DOUBLE || that.kind == IRPrimitiveKind.FLOAT ||
                    that.kind == IRPrimitiveKind.LONG || that.kind == IRPrimitiveKind.INT ||
                    that.kind == IRPrimitiveKind.SHORT || that.kind == IRPrimitiveKind.BYTE;
        }
        if(kind == IRPrimitiveKind.FLOAT) {
            return  that.kind == IRPrimitiveKind.FLOAT ||
                    that.kind == IRPrimitiveKind.LONG || that.kind == IRPrimitiveKind.INT ||
                    that.kind == IRPrimitiveKind.SHORT || that.kind == IRPrimitiveKind.BYTE;
        }
        if(kind == IRPrimitiveKind.LONG) {
            return that.kind == IRPrimitiveKind.LONG || that.kind == IRPrimitiveKind.INT ||
                    that.kind == IRPrimitiveKind.SHORT || that.kind == IRPrimitiveKind.BYTE;
        }
        if(kind == IRPrimitiveKind.INT) {
            return that.kind == IRPrimitiveKind.INT ||
                    that.kind == IRPrimitiveKind.SHORT || that.kind == IRPrimitiveKind.BYTE;
        }
        if(kind == IRPrimitiveKind.SHORT) {
            return that.kind == IRPrimitiveKind.SHORT || that.kind == IRPrimitiveKind.BYTE;
        }
        return kind == that.kind;
    }

}
