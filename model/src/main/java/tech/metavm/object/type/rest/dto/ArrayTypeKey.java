package tech.metavm.object.type.rest.dto;

import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;

public record ArrayTypeKey(int kind, TypeKey elementTypeKey) implements TypeKey {

    private static final int[] typeKeyCodes = new int[4];

    static {
        typeKeyCodes[ArrayKind.READ_ONLY.code()] = TypeKeyCodes.READ_ONLY_ARRAY;
        typeKeyCodes[ArrayKind.READ_WRITE.code()] = TypeKeyCodes.READ_WRITE_ARRAY;
        typeKeyCodes[ArrayKind.CHILD.code()] = TypeKeyCodes.CHILD_ARRAY;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(typeKeyCodes[kind]);
        elementTypeKey.write(output);
    }

    @Override
    public String toTypeExpression() {
        return elementTypeKey.toTypeExpression() + ArrayKind.fromCode(kind).getSuffix().toLowerCase();
    }

    @Override
    public ArrayType toType(TypeDefProvider typeDefProvider) {
        return new ArrayType(elementTypeKey.toType(typeDefProvider), ArrayKind.fromCode(kind));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitArrayTypeKey(this);
    }

    @Override
    public void acceptChildren(TypeKeyVisitor<?> visitor) {
        elementTypeKey.accept(visitor);
    }

    @Override
    public int getCode() {
        return getTypeKeyCode(kind);
    }

    public static int getTypeKeyCode(int kind) {
        return typeKeyCodes[kind];
    }

}
