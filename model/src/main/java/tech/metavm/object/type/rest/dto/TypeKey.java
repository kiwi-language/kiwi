package tech.metavm.object.type.rest.dto;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.type.antlr.TypeLexer;
import tech.metavm.object.type.antlr.TypeParser;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface TypeKey {

    void write(InstanceOutput output);

    String toTypeExpression();

    Type toType(TypeDefProvider typeDefProvider);

    <R> R accept(TypeKeyVisitor<R> visitor);

    void acceptChildren(TypeKeyVisitor<?> visitor);

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
            var classType = ctx.classType();
            var id = classType.qualifiedName().getText().substring(Constants.CONSTANT_ID_PREFIX.length());
            if(classType.typeArguments() != null)
                return new ParameterizedTypeKey(id, NncUtils.map(classType.typeArguments().typeList().type(), TypeKey::fromTypeContext));
            else
                return new ClassTypeKey(id);
        }
        if(ctx.variableType() != null)
            return new VariableTypeKey(ctx.variableType().IDENTIFIER().getText().substring(Constants.CONSTANT_ID_PREFIX.length()));
        if (ctx.elementType != null) {
            var kind = ctx.arrayKind();
            return new ArrayTypeKey(
                    kind == null ? ArrayKind.READ_WRITE.code() :
                            (kind.R() != null ? ArrayKind.READ_ONLY.code() : ArrayKind.CHILD.code()),
                    fromTypeContext(ctx.elementType)
            );
        }
        if(ctx.LBRACK() != null)
            return new UncertainTypeKey(fromTypeContext(ctx.type(0)), fromTypeContext(ctx.type(1)));
        if(ctx.ARROW() != null) {
            return new FunctionTypeKey(
                    ctx.typeList() != null ? NncUtils.map(ctx.typeList().type(), TypeKey::fromTypeContext) : List.of(),
                    fromTypeContext(ctx.type(0))
            );
        }
        if(ctx.BITAND() != null)
            return new IntersectionTypeKey(NncUtils.mapUnique(ctx.type(), TypeKey::fromTypeContext));
        if(ctx.BITOR() != null)
            return new UnionTypeKey(NncUtils.mapUnique(ctx.type(), TypeKey::fromTypeContext));
        if(ctx.NEVER() != null)
            return new NeverTypeKey();
        if(ctx.ANY() != null)
            return new AnyTypeKey();
        throw new InternalException("Invalid type: " + ctx.getText());
    }



    static TypeKey read(InstanceInput input) {
        int code = input.read();
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
            case TypeKeyCodes.CLASS -> new ClassTypeKey(input.readId().toString());
            case TypeKeyCodes.PARAMETERIZED ->
                    new ParameterizedTypeKey(input.readId().toString(), readTypeKeyList(input));
            case TypeKeyCodes.UNION -> new UnionTypeKey(readTypeKeySet(input));
            case TypeKeyCodes.INTERSECTION -> new IntersectionTypeKey(readTypeKeySet(input));
            case TypeKeyCodes.FUNCTION -> new FunctionTypeKey(readTypeKeyList(input), read(input));
            case TypeKeyCodes.UNCERTAIN -> new UncertainTypeKey(read(input), read(input));
            case TypeKeyCodes.VARIABLE -> new VariableTypeKey(input.readId().toString());
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
