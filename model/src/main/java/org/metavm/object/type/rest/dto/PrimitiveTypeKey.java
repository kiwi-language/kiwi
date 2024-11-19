package org.metavm.object.type.rest.dto;

import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.WireTypes;

public record PrimitiveTypeKey(int kind) implements TypeKey {

    private static final int[] typeKeyCodesMap;

    static {
        int maxPrimKind = 0;
        for (PrimitiveKind kind : PrimitiveKind.values()) {
            maxPrimKind = Math.max(maxPrimKind, kind.code());
        }
        typeKeyCodesMap = new int[maxPrimKind + 1];
        for (PrimitiveKind kind : PrimitiveKind.values()) {
            var field = ReflectionUtils.getField(WireTypes.class, kind.name() + "_TYPE");
            typeKeyCodesMap[kind.code()] = ReflectionUtils.getIntField(field, null);
        }
    }

    @Override
    public void write(MvOutput output) {
        output.write(typeKeyCodesMap[kind]);
    }

    @Override
    public String toTypeExpression() {
        return PrimitiveKind.fromCode(kind).name().toLowerCase();
    }

    @Override
    public PrimitiveType toType(TypeDefProvider typeDefProvider) {
        return PrimitiveKind.fromCode(kind).getType();
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitPrimitiveTypeKey(this);
    }

    @Override
    public int getCode() {
        return getTypeKeyCode(kind);
    }

    public static int getTypeKeyCode(int kind) {
        return typeKeyCodesMap[kind];
    }

}
