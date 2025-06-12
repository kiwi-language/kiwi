package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.Formattable;
import org.metavm.compiler.diag.Messages;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Predicate;

public enum TokenKind implements Formattable, Predicate<TokenKind> {
    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    AND("&&"),
    OR("||"),
    EQ("=="),
    NE("!="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">=") {
        @Override
        public TokenKind[] split() {
            return new TokenKind[] {GT, ASSIGN};
        }
    },
    NOT("!"),
    NONNULL("!!"),
    SHL("<<"),
    SHR(">>") {
        @Override
        public TokenKind[] split() {
            return new TokenKind[] {GT, GT};
        }
    },
    USHR(">>>") {
        @Override
        public TokenKind[] split() {
            return new TokenKind[] {GT, SHR};
        }
    },
    BITAND("&"),
    BITOR("|"),
    BITXOR("^"),
    BITNOT("~"),

    INC("++"),
    DEC("--"),
    ASSIGN("="),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/="),
    MOD_ASSIGN("%="),
    BITAND_ASSIGN("&="),
    BITOR_ASSIGN("|="),
    BITXOR_ASSIGN("^="),
    SHL_ASSIGN("<<="),
    SHR_ASSIGN(">>="),
    USHR_ASSIGN(">>>="),
    AND_ASSIGN("&&="),
    OR_ASSIGN("||="),
    LPAREN("("),
    RPAREN(")"),
    LBRACKET("["),
    RBRACKET("]"),
    LBRACE("{"),
    RBRACE("}"),
    COMMA(","),
    COLON(":"),
    SEMICOLON(";"),
    DOT("."),
    AT("@"),
    ARROW("->"),
    QUES("?"),
    ELLIPSIS("..."),
    IDENT,
    STRING_LIT,
    CHAR_LIT,
    INTEGER_LIT,
    LONG_LIT,
    FLOAT_LIT,
    DOUBLE_LIT,
    ERROR,
    EOF,
    IN(true),
    // keywords
    PUB(true),
    PROT(true),
    PRIV(true),
    NATIVE(true),
    STATIC(true),
    DELETED(true),
    FN(true),
    VAR(true),
    VAL(true),
    TEMP(true),
    VALUE(true),
    ABSTRACT(true),
    CLASS(true),
    INTERFACE(true),
    ENUM(true),
    IMPORT(true),
    PACKAGE(true),
    VOID(true),
    BOOL(true),
    BYTE(true),
    SHORT(true),
    INT(true),
    LONG(true),
    FLOAT(true),
    DOUBLE(true),
    CHAR(true),
    STRING(true),
    ANY(true),
    NEVER(true),
    TIME(true),
    PASSWORD(true),
    IF(true),
    ELSE(true),
    SWITCH(true),
    CASE(true),
    DEFAULT(true),
    WHILE(true),
    DO(true),
    FOR(true),
    BREAK(true),
    CONTINUE(true),
    RETURN(true),
    THROW(true),
    TRY(true),
    CATCH(true),
    FINALLY(true),
    ASSERT(true),
    IS(true),
    AS(true),
    THIS(true),
    SUPER(true),
    NEW(true),
    DELETE(true),
    TRUE(true),
    FALSE(true),
    NULL(true)


    ;

    private final @Nullable String op;
    private final boolean isKeyword;

    TokenKind() {
        this(false);
    }

    TokenKind(boolean isKeyword) {
        this(isKeyword, null);
    }

    TokenKind(@Nullable String op) {
        this(false, op);
    }

    TokenKind(boolean isKeyword, @Nullable String op) {
        this.isKeyword = isKeyword;
        this.op = op;
    }

    public boolean isKeyword() {
        return isKeyword;
    }

    @Override
    public String toString(Locale locale, Messages messages) {
        return op != null ? op : name().toLowerCase();
    }

    public TokenKind[] split() {
        throw new UnsupportedOperationException("Cannot split token " + name());
    }

    @Override
    public boolean test(TokenKind tokenKind) {
        return this == tokenKind;
    }
}
