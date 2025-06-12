package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;

import java.util.HashMap;
import java.util.Map;

public class Tokens {
    public static final Token EOF = new Token(TokenKind.EOF, -1, -1);

    private static final Map<Name, TokenKind> keywords;

    static {
        var kw = new HashMap<Name, TokenKind>();
        for (TokenKind tk : TokenKind.values()) {
            if (tk.isKeyword())
                kw.put(Name.from(tk.name().toLowerCase()), tk);
        }
        keywords = new HashMap<>(kw);
    }

    public static TokenKind lookupKind(Name name) {
        return keywords.getOrDefault(name, TokenKind.IDENT);
    }

}
