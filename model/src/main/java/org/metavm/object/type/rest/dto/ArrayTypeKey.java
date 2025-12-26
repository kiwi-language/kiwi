package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.jsonk.JsonIgnore;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.TypeTags;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Json
public record ArrayTypeKey(int arrayKind, TypeKey elementTypeKey) implements TypeKey {

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
        output.write(typeKeyCodes[arrayKind]);
        elementTypeKey.write(output);
    }

    @Override
    public String toTypeExpression() {
        return elementTypeKey.toTypeExpression() + ArrayKind.fromCode(arrayKind).getSuffix().toLowerCase();
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitArrayTypeKey(this);
    }

    @Override
    public int getCode() {
        return getTypeKeyCode(arrayKind);
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
        return typeTags[arrayKind];
    }
}
