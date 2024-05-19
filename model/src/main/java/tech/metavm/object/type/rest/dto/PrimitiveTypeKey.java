package tech.metavm.object.type.rest.dto;

import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.ReflectionUtils;

public record PrimitiveTypeKey(int kind) implements TypeKey {

    private static final int[] typeKeyCodesMap;

    static {
        int maxPrimKind = 0;
        for (PrimitiveKind kind : PrimitiveKind.values()) {
            maxPrimKind = Math.max(maxPrimKind, kind.code());
        }
        typeKeyCodesMap = new int[maxPrimKind + 1];
        for (PrimitiveKind kind : PrimitiveKind.values()) {
            var field = ReflectionUtils.getField(TypeKeyCodes.class, kind.name());
            typeKeyCodesMap[kind.code()] = ReflectionUtils.getIntField(field, null);
        }
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(typeKeyCodesMap[kind]);
    }

    @Override
    public String toTypeExpression() {
        return PrimitiveKind.fromCode(kind).name().toLowerCase();
    }

    @Override
    public PrimitiveType toType(TypeDefProvider typeDefProvider) {
        return new PrimitiveType(PrimitiveKind.fromCode(kind));
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
