package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.util.InternalException;

public interface TypeParser {

    static Type parse(@NotNull String expression, TypeDefProvider typeDefProvider) {
        try {
            return new TypeParserImpl(typeDefProvider).parse(expression);
        }
        catch (Exception e) {
            throw new InternalException("Fail to parse type expression " + expression, e);
        }
    }

    Type parse(String expression);

}
