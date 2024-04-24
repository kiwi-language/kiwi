package tech.metavm.object.type;

public interface TypeParser {

    static Type parse(String expression, TypeDefProvider typeDefProvider) {
        return new TypeParserImpl(typeDefProvider).parse(expression);
    }

    Type parse(String expression);

}
