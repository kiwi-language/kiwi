package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.flow.FunctionSignature;
import org.metavm.flow.SimpleMethodRef;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.InternalException;

public interface TypeParser {

    static KlassType parseClassType(@NotNull String expression, TypeDefProvider typeDefProvider) {
        var type = parseType(expression, typeDefProvider);
        if(type instanceof KlassType classType)
            return classType;
        throw new RuntimeException(expression + " is not a class type");
    }

    static Type parseType(@NotNull String expression, TypeDefProvider typeDefProvider) {
        try {
            return new TypeParserImpl(typeDefProvider).parseType(expression);
        }
        catch (Exception e) {
            throw new InternalException("Fail to parse type expression " + expression, e);
        }
    }


    TypeKey parseTypeKey(@NotNull String expression);

    static SimpleMethodRef parseSimpleMethodRef(@NotNull String expression, ParserTypeDefProvider typeDefProvider) {
        try {
            return new TypeParserImpl(typeDefProvider).parseSimpleMethodRef(expression);
        }
        catch (Exception e) {
            throw new InternalException("Fail to parse type expression " + expression, e);
        }
    }

    Type parseType(String expression);

    FunctionSignature parseFunctionSignature(String expression);

    SimpleMethodRef parseSimpleMethodRef(String expression);
}
