package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.MethodRef;
import org.metavm.flow.SimpleMethodRef;
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

    static MethodRef parseMethodRef(@NotNull String expression, TypeDefProvider typeDefProvider) {
        try {
            return new TypeParserImpl(typeDefProvider).parseMethodRef(expression);
        }
        catch (Exception e) {
            throw new InternalException("Fail to parse type expression " + expression, e);
        }
    }

    static SimpleMethodRef parseSimpleMethodRef(@NotNull String expression, ParserTypeDefProvider typeDefProvider) {
        try {
            return new TypeParserImpl(typeDefProvider).parseSimpleMethodRef(expression);
        }
        catch (Exception e) {
            throw new InternalException("Fail to parse type expression " + expression, e);
        }
    }

    Type parseType(String expression);

    MethodRef parseMethodRef(String expression);

    MethodRef parseMethodRef(org.metavm.object.type.antlr.TypeParser.MethodRefContext ctx);

    FunctionRef parseFunctionRef(String expression);

    SimpleMethodRef parseSimpleMethodRef(String expression);
}
