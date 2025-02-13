package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeTags;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record ArrayTypeKey(int kind, TypeKey elementTypeKey) implements TypeKey {

    private static final int[] typeKeyCodes = new int[5];
    private static final int[] typeTags = new int[5];

    static {
        typeKeyCodes[ArrayKind.READ_ONLY.code()] = WireTypes.READ_ONLY_ARRAY_TYPE;
        typeKeyCodes[ArrayKind.DEFAULT.code()] = WireTypes.ARRAY_TYPE;
        typeTags[ArrayKind.READ_ONLY.code()] = TypeTags.READONLY_ARRAY;
        typeTags[ArrayKind.DEFAULT.code()] = TypeTags.ARRAY;
    }

    @Override
    public void write(MvOutput output) {
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
