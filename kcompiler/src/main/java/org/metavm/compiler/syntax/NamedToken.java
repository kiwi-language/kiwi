package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;

public class NamedToken extends Token {

    private final Name name;

    public NamedToken(TokenKind kind, int start, int end, Name name) {
        super(kind, start, end);
        this.name = name;
    }

    public Name getName() {
        return name;
    }
}
