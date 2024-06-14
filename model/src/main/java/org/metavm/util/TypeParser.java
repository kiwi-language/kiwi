package org.metavm.util;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public class TypeParser {

    public static Type parse(String typeName) {
        return new TypeParser(typeName).readClassOrParameterized();
    }

    private final String typeName;
    private int position;

    public TypeParser(String typeName) {
        this.typeName = typeName;
    }

    Type readClassOrParameterized() {
        Class<?> rawClass = readClass();
        skipWhitespaces();
        if(isEof() || isComma() || isGreaterThan()) {
            return rawClass;
        }
        if(!isLessThan()) {
            throw invalidTypeName();
        }
        advanceAndSkipWhitespaces();
        return new ParameterizedTypeImpl(
                null,
                rawClass,
                readTypeArguments()
        );
    }

    private Type[] readTypeArguments() {
        List<Type> types = new ArrayList<>();
        while (true) {
            types.add(readType());
            skipWhitespaces();
            if(isEof()) {
                throw invalidTypeName();
            }
            if(isComma()) {
                advanceAndSkipWhitespaces();
                continue;
            }
            if(isGreaterThan()) {
                Type[] typeArray = new Type[types.size()];
                types.toArray(typeArray);
                return typeArray;
            }
            throw invalidTypeName();
        }
    }

    private Type readType() {
        if(charAt() == '?') {
            return readWildCard();
        }
        else {
            return readClassOrParameterized();
        }
    }

    private WildcardType readWildCard() {
        if(charAt() != '?') {
            throw invalidTypeName();
        }
        advanceAndSkipWhitespaces();
        if(isTypeEnd()) {
            return new WildcardTypeImpl(new Type[0], new Type[0]);
        }
        if(tokenEquals("extends")) {
            advanceAndSkipWhitespaces("extends".length());
            return WildcardTypeImpl.createExtends(readClassOrParameterized());
        }
        if(tokenEquals("super")) {
            advanceAndSkipWhitespaces("super".length());
            return WildcardTypeImpl.createSuper(readClassOrParameterized());
        }
        throw invalidTypeName();
    }

    private void advanceAndSkipWhitespaces() {
        advanceAndSkipWhitespaces(1);
    }

    private void advanceAndSkipWhitespaces(int chars) {
        position += chars;
        skipWhitespaces();
    }

    private boolean tokenEquals(String str) {
        return typeName.startsWith(str, position);
    }

    private Class<?> readClass() {
        try {
            return Class.forName(readClassName());
        } catch (ClassNotFoundException e) {
            throw invalidTypeName(e);
        }
    }

    private String readClassName() {
        int start = position;
        while (!isTypeEnd()) {
            position++;
        }
        return typeName.substring(start, position);
    }

    private boolean isTypeEnd() {
        return isEof() || isWhitespace() || isLessThan() || isGreaterThan() || isComma();
    }

    private void skipWhitespaces() {
        while (!isEof() && isWhitespace()) {
            position++;
        }
    }

    private boolean isWhitespace() {
        char c = charAt();
        return c == '\t' || c == '\n' ||  c == ' ' || c == '\0' || c == '\r';
    }

    private boolean isLessThan() {
        return charAt() == '<';
    }

    private boolean isGreaterThan() {
        return charAt() == '>';
    }

    private boolean isComma() {
        return charAt() == ',';
    }

    private boolean isEof() {
        return position >= typeName.length();
    }

    private char charAt() {
        if(isEof()) {
            throw invalidTypeName();
        }
        return typeName.charAt(position);
    }

    private InternalException invalidTypeName() {
        return invalidTypeName(null);
    }

    private InternalException invalidTypeName(Throwable cause) {
        return new InternalException("Invalid typeName '" + typeName + "'", cause);
    }
}
