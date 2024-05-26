package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.Method;
import tech.metavm.flow.MethodRef;
import tech.metavm.util.InternalException;

public interface TypeParser {

    static ClassType parseClassType(@NotNull String expression, TypeDefProvider typeDefProvider) {
        var type = parseType(expression, typeDefProvider);
        if(type instanceof ClassType classType)
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

    Type parseType(String expression);

    MethodRef parseMethodRef(String expression);

}
