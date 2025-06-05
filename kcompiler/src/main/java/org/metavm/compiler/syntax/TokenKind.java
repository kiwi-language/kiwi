package org.metavm.compiler.syntax;

public enum TokenKind {
    PLUS, MINUS, MUL, DIV,MOD,
    AND,
    OR,
    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,
    NOT,
    SHL,
    SHR,
    USHR,
    BITAND,
    BITOR,
    BITXOR,
    BITNOT,

    INC, DEC,
    ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MUL_ASSIGN, DIV_ASSIGN,
    MOD_ASSIGN, BITAND_ASSIGN, BITOR_ASSIGN, BITXOR_ASSIGN,
    SHL_ASSIGN, SHR_ASSIGN,USHR_ASSIGN, AND_ASSIGN, OR_ASSIGN,

    LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE,

    IDENT, STRING_LIT, CHAR_LIT, INTEGER_LIT, LONG_LIT, FLOAT_LIT, DOUBLE_LIT,
    ERROR,
    EOF,

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
    IF(true),
    ELSE(true),
    SWITCH(true),
    CASE(true),
    DEFAULT(true),
    WHILE(true),
    DO(true),
    FOR(true),
    IN(true),
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

    private final boolean isKeyword;

    TokenKind() {
        this(false);
    }

    TokenKind(boolean isKeyword) {
        this.isKeyword = isKeyword;
    }

    public boolean isKeyword() {
        return isKeyword;
    }
}
