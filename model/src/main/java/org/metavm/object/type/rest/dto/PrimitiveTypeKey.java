package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.util.MvOutput;

import static org.metavm.util.WireTypes.*;

@Json
public record PrimitiveTypeKey(int primitiveKind) implements TypeKey {

    private static final int[] typeKeyCodesMap = new int[] {
            0,
            LONG_TYPE,
            DOUBLE_TYPE,
            0,
            BOOLEAN_TYPE,
            TIME_TYPE,
            PASSWORD_TYPE,
            0,
            VOID_TYPE,
            CHAR_TYPE,
            INT_TYPE,
            FLOAT_TYPE,
            SHORT_TYPE,
            BYTE_TYPE
    };

    @Override
    public void write(MvOutput output) {
        output.write(typeKeyCodesMap[primitiveKind]);
    }

    @Override
    public String toTypeExpression() {
        return PrimitiveKind.fromCode(primitiveKind).name().toLowerCase();
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitPrimitiveTypeKey(this);
    }

    @Override
    public int getCode() {
        return getTypeKeyCode(primitiveKind);
    }

    public static int getTypeKeyCode(int kind) {
        return typeKeyCodesMap[kind];
    }

}
