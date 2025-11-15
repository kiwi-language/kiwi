package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.jsonk.JsonIgnore;
import org.metavm.flow.rest.FunctionRefKey;
import org.metavm.flow.rest.MethodRefKey;
import org.metavm.object.type.*;
import org.metavm.util.InstanceInput;
import org.metavm.util.InternalException;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Json(
        typeProperty = "kind",
        subTypes = {
                @org.jsonk.SubType(value = "primitive", type = PrimitiveTypeKey.class),
                @org.jsonk.SubType(value = "string", type = StringTypeKey.class),
                @org.jsonk.SubType(value = "null", type = NullTypeKey.class),
                @org.jsonk.SubType(value = "array", type = ArrayTypeKey.class),
                @org.jsonk.SubType(value = "class", type = ClassTypeKey.class),
                @org.jsonk.SubType(value = "parameterized", type = ParameterizedTypeKey.class),
                @org.jsonk.SubType(value = "union", type = UnionTypeKey.class),
                @org.jsonk.SubType(value = "intersection", type = IntersectionTypeKey.class),
                @org.jsonk.SubType(value = "function", type = FunctionTypeKey.class),
                @org.jsonk.SubType(value = "uncertain", type = UncertainTypeKey.class),
                @org.jsonk.SubType(value = "variable", type = VariableTypeKey.class),
                @org.jsonk.SubType(value = "captured", type = CapturedTypeKey.class),
                @org.jsonk.SubType(value = "never", type = NeverTypeKey.class),
                @org.jsonk.SubType(value = "any", type = AnyTypeKey.class),
        }
)
public interface TypeKey extends TypeOrTypeKey {

    void write(MvOutput output);

    String toTypeExpression();

    <R> R accept(TypeKeyVisitor<R> visitor);

    int getCode();

    @Override
    @JsonIgnore
    default int getTypeTag() {
        return 0;
    }

    @Override
    @JsonIgnore
    default boolean isArray() {
        return false;
    }

    static TypeKey fromExpression(String expression) {
        return new TypeParserImpl((TypeDefProvider) id -> {
            throw new UnsupportedOperationException();
        }).parseTypeKey(expression);
    }

    static TypeKey read(InstanceInput input) {
        int code = input.read();
        return read(code, input);
    }

    static @Nullable GenericDeclarationRefKey readGenericDeclarationRef(InstanceInput input) {
        var code = input.read();
        return switch (code) {
            case WireTypes.NULL -> null;
            case WireTypes.METHOD_REF -> MethodRefKey.read(input);
            case WireTypes.FUNCTION_REF -> FunctionRefKey.read(input);
            default -> (GenericDeclarationRefKey) read(code, input);
        };
    }

    static TypeKey read(int code, InstanceInput input) {
        return switch (code) {
            case WireTypes.ANY_TYPE -> new AnyTypeKey();
            case WireTypes.NEVER_TYPE -> new NeverTypeKey();
            case WireTypes.BOOLEAN_TYPE -> new PrimitiveTypeKey(PrimitiveKind.BOOLEAN.code());
            case WireTypes.LONG_TYPE -> new PrimitiveTypeKey(PrimitiveKind.LONG.code());
            case WireTypes.INT_TYPE -> new PrimitiveTypeKey(PrimitiveKind.INT.code());
            case WireTypes.CHAR_TYPE -> new PrimitiveTypeKey(PrimitiveKind.CHAR.code());
            case WireTypes.SHORT_TYPE -> new PrimitiveTypeKey(PrimitiveKind.SHORT.code());
            case WireTypes.BYTE_TYPE -> new PrimitiveTypeKey(PrimitiveKind.BYTE.code());
            case WireTypes.DOUBLE_TYPE -> new PrimitiveTypeKey(PrimitiveKind.DOUBLE.code());
            case WireTypes.FLOAT_TYPE -> new PrimitiveTypeKey(PrimitiveKind.FLOAT.code());
            case WireTypes.STRING_TYPE -> new StringTypeKey();
            case WireTypes.VOID_TYPE -> new PrimitiveTypeKey(PrimitiveKind.VOID.code());
            case WireTypes.NULL_TYPE -> new NullTypeKey();
            case WireTypes.TIME_TYPE -> new PrimitiveTypeKey(PrimitiveKind.TIME.code());
            case WireTypes.PASSWORD_TYPE -> new PrimitiveTypeKey(PrimitiveKind.PASSWORD.code());
            case WireTypes.READ_ONLY_ARRAY_TYPE -> new ArrayTypeKey(ArrayKind.READ_ONLY.code(), read(input));
            case WireTypes.ARRAY_TYPE -> new ArrayTypeKey(ArrayKind.DEFAULT.code(), read(input));
            case WireTypes.CLASS_TYPE -> new ClassTypeKey(input.readId());
            case WireTypes.PARAMETERIZED_TYPE ->
                    new ParameterizedTypeKey(readGenericDeclarationRef(input), input.readId(), readTypeKeyList(input));
            case WireTypes.UNION_TYPE -> new UnionTypeKey(readTypeKeySet(input));
            case WireTypes.INTERSECTION_TYPE -> new IntersectionTypeKey(readTypeKeySet(input));
            case WireTypes.FUNCTION_TYPE -> new FunctionTypeKey(readTypeKeyList(input), read(input));
            case WireTypes.UNCERTAIN_TYPE -> new UncertainTypeKey(read(input), read(input));
            case WireTypes.VARIABLE_TYPE -> new VariableTypeKey(input.readId());
            case WireTypes.CAPTURED_TYPE -> new CapturedTypeKey(input.readId());
            default -> throw new InternalException("Invalid type key code: " + code);
        };
    }

    static Set<TypeKey> readTypeKeySet(InstanceInput input) {
        var num = input.readInt();
        var typeKeys = new HashSet<TypeKey>(num);
        for (int i = 0; i < num; i++)
            typeKeys.add(read(input));
        return typeKeys;
    }

    static List<TypeKey> readTypeKeyList(InstanceInput input) {
        var num = input.readInt();
        var typeKeys = new ArrayList<TypeKey>(num);
        for (int i = 0; i < num; i++)
            typeKeys.add(read(input));
        return typeKeys;
    }

}
