package org.manul.compiler.syntax;

import org.manul.compiler.diag.Messages;
import org.manul.compiler.element.Name;

import java.util.Locale;

public class NamedToken extends Token {

    private final Name name;

    public NamedToken(TokenKind kind, int start, int end, Name name) {
        super(kind, start, end);
        this.name = name;
    }

    public Name getName() {
        return name;
    }

    @Override
    public String toString(Locale locale, Messages messages) {
        return name.toString();
    }
}
