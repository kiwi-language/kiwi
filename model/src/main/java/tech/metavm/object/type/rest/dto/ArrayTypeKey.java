package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.type.*;
import tech.metavm.util.InstanceOutput;

public record ArrayTypeKey(int kind, TypeKey elementTypeKey) implements TypeKey {

    private static final int[] typeKeyCodes = new int[4];
    private static final int[] typeTags = new int[4];

    static {
        typeKeyCodes[ArrayKind.READ_ONLY.code()] = TypeKeyCodes.READ_ONLY_ARRAY;
        typeKeyCodes[ArrayKind.READ_WRITE.code()] = TypeKeyCodes.READ_WRITE_ARRAY;
        typeKeyCodes[ArrayKind.CHILD.code()] = TypeKeyCodes.CHILD_ARRAY;
        typeTags[ArrayKind.READ_ONLY.code()] = TypeTags.READONLY_ARRAY;
        typeTags[ArrayKind.READ_WRITE.code()] = TypeTags.READ_WRITE_ARRAY;
        typeTags[ArrayKind.CHILD.code()] = TypeTags.CHILD_ARRAY;
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
    public int getCode() {
        return getTypeKeyCode(kind);
    }

    public static int getTypeKeyCode(int kind) {
        return typeKeyCodes[kind];
    }

    @Override
    @JsonIgnore
    public boolean isArray() {
        return true;
    }

    @Override
    public int getTypeTag() {
        return typeTags[kind];
    }
}
