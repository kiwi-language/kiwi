package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;

import java.util.Map;

public class Tokens {
    public static final Token EOF = new Token(TokenKind.EOF, -1, -1);

    private static Map<Name, TokenKind> keywords = Map.ofEntries(
    );

    public static TokenKind lookupKind(Name name) {
        return keywords.getOrDefault(name, TokenKind.IDENTIFIER);
    }

}
