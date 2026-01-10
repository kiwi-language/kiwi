package org.manul.object.type;

import org.jetbrains.annotations.NotNull;
import org.manul.flow.FunctionSignature;
import org.manul.flow.SimpleMethodRef;
import org.manul.object.type.rest.dto.TypeKey;
import org.manul.util.InternalException;

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
