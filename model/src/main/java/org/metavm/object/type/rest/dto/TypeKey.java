package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.object.type.antlr.TypeLexer;
import org.metavm.object.type.antlr.TypeParser;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface TypeKey extends TypeOrTypeKey {

    void write(InstanceOutput output);

    String toTypeExpression();

    Type toType(TypeDefProvider typeDefProvider);

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
        var parser = new TypeParser(new CommonTokenStream(new TypeLexer(CharStreams.fromString(expression))));
        return fromTypeContext(parser.type());
    }

    static TypeKey fromTypeContext(TypeParser.TypeContext ctx) {
        if (ctx.primitiveType() != null) {
            var primitiveType = ctx.primitiveType();
            if (primitiveType.LONG() != null)
                return new PrimitiveTypeKey(PrimitiveKind.LONG.code());
            if (primitiveType.DOUBLE() != null)
                return new PrimitiveTypeKey(PrimitiveKind.DOUBLE.code());
            if (primitiveType.STRING() != null)
                return new PrimitiveTypeKey(PrimitiveKind.STRING.code());
            if (primitiveType.BOOLEAN() != null)
                return new PrimitiveTypeKey(PrimitiveKind.BOOLEAN.code());
            if (primitiveType.NULL() != null)
                return new PrimitiveTypeKey(PrimitiveKind.NULL.code());
            if (primitiveType.VOID() != null)
                return new PrimitiveTypeKey(PrimitiveKind.VOID.code());
            if (primitiveType.PASSWORD() != null)
                return new PrimitiveTypeKey(PrimitiveKind.PASSWORD.code());
            if (primitiveType.TIME() != null)
                return new PrimitiveTypeKey(PrimitiveKind.TIME.code());
            throw new InternalException("Invalid primitive type: " + primitiveType.getText());
        }
        if (ctx.classType() != null) {
            return fromClassTypeContext(ctx.classType());
        }
        if(ctx.variableType() != null) {
            var variableType = ctx.variableType();
            return new VariableTypeKey(
                    GenericDeclarationRefKey.fromContext(variableType.genericDeclarationRef()),
                    Id.parse(Constants.removeIdPrefix(variableType.IDENTIFIER().getText()))
            );
        }
//            return new VariableTypeKey(Id.parse(ctx.variableType().qualifiedName().getText().substring(Constants.ID_PREFIX.length())));
        if (ctx.elementType != null) {
            var kind = ctx.arrayKind();
            return new ArrayTypeKey(parseArrayKind(kind).code(), fromTypeContext(ctx.elementType));
        }
        if(ctx.LBRACK() != null)
            return new UncertainTypeKey(fromTypeContext(ctx.type(0)), fromTypeContext(ctx.type(1)));
        if(ctx.ARROW() != null) {
            return new FunctionTypeKey(
                    ctx.typeList() != null ? NncUtils.map(ctx.typeList().type(), TypeKey::fromTypeContext) : List.of(),
                    fromTypeContext(ctx.type(0))
            );
        }
        if(!ctx.BITAND().isEmpty())
            return new IntersectionTypeKey(NncUtils.mapUnique(ctx.type(), TypeKey::fromTypeContext));
        if(!ctx.BITOR().isEmpty())
            return new UnionTypeKey(NncUtils.mapUnique(ctx.type(), TypeKey::fromTypeContext));
        if(ctx.NEVER() != null)
            return new NeverTypeKey();
        if(ctx.ANY() != null)
            return new AnyTypeKey();
        throw new InternalException("Invalid type: " + ctx.getText());
    }

    static TypeKey fromClassTypeContext(TypeParser.ClassTypeContext classType) {
        var id = Id.parse(classType.qualifiedName().getText().substring(Constants.ID_PREFIX.length()));
        if(classType.typeArguments() != null)
            return new ParameterizedTypeKey(id, NncUtils.map(classType.typeArguments().typeList().type(), TypeKey::fromTypeContext));
        else if(classType.DECIMAL_LITERAL() != null)
            return new TaggedClassTypeKey(id, Integer.parseInt(classType.DECIMAL_LITERAL().getText()));
        else
            return new ClassTypeKey(id);
    }

    static TypeKey read(InstanceInput input) {
        int code = input.read();
        return read(code, input);
    }

    static TypeKey read(int code, InstanceInput input) {
        return switch (code) {
            case TypeKeyCodes.ANY -> new AnyTypeKey();
            case TypeKeyCodes.NEVER -> new NeverTypeKey();
            case TypeKeyCodes.BOOLEAN -> new PrimitiveTypeKey(PrimitiveKind.BOOLEAN.code());
            case TypeKeyCodes.LONG -> new PrimitiveTypeKey(PrimitiveKind.LONG.code());
            case TypeKeyCodes.DOUBLE -> new PrimitiveTypeKey(PrimitiveKind.DOUBLE.code());
            case TypeKeyCodes.STRING -> new PrimitiveTypeKey(PrimitiveKind.STRING.code());
            case TypeKeyCodes.VOID -> new PrimitiveTypeKey(PrimitiveKind.VOID.code());
            case TypeKeyCodes.NULL -> new PrimitiveTypeKey(PrimitiveKind.NULL.code());
            case TypeKeyCodes.TIME -> new PrimitiveTypeKey(PrimitiveKind.TIME.code());
            case TypeKeyCodes.PASSWORD -> new PrimitiveTypeKey(PrimitiveKind.PASSWORD.code());
            case TypeKeyCodes.CHILD_ARRAY -> new ArrayTypeKey(ArrayKind.CHILD.code(), read(input));
            case TypeKeyCodes.READ_ONLY_ARRAY -> new ArrayTypeKey(ArrayKind.READ_ONLY.code(), read(input));
            case TypeKeyCodes.READ_WRITE_ARRAY -> new ArrayTypeKey(ArrayKind.READ_WRITE.code(), read(input));
            case TypeKeyCodes.VALUE_ARRAY -> new ArrayTypeKey(ArrayKind.VALUE.code(), read(input));
            case TypeKeyCodes.CLASS -> new ClassTypeKey(input.readId());
            case TypeKeyCodes.TAGGED_CLASS -> new TaggedClassTypeKey(input.readId(), input.readInt());
            case TypeKeyCodes.PARAMETERIZED ->
                    new ParameterizedTypeKey(input.readId(), readTypeKeyList(input));
            case TypeKeyCodes.UNION -> new UnionTypeKey(readTypeKeySet(input));
            case TypeKeyCodes.INTERSECTION -> new IntersectionTypeKey(readTypeKeySet(input));
            case TypeKeyCodes.FUNCTION -> new FunctionTypeKey(readTypeKeyList(input), read(input));
            case TypeKeyCodes.UNCERTAIN -> new UncertainTypeKey(read(input), read(input));
            case TypeKeyCodes.VARIABLE -> new VariableTypeKey(GenericDeclarationRefKey.read(input), input.readId());
            case TypeKeyCodes.CAPTURED -> new CapturedTypeKey(input.readId());
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

    private static ArrayKind parseArrayKind(@Nullable TypeParser.ArrayKindContext ctx) {
        if(ctx == null)
            return ArrayKind.READ_WRITE;
        if(ctx.R() != null)
            return ArrayKind.READ_ONLY;
        if(ctx.C() != null)
            return ArrayKind.CHILD;
        if(ctx.V() != null)
            return ArrayKind.VALUE;
        throw new InternalException("Unrecognized array kind: " + ctx.getText());
    }

}
