package tech.metavm.transpile;

import tech.metavm.util.NncUtils;

public enum TsLexicon {

    DEFAULT("default"),
    PUBLIC("public"),
    EXPORT("export"),
    OPEN_ANGULAR_BRACKET("<", true, true),
    CLOSING_ANGULAR_BRACKET(">", true, false),
    COMMA(",", true, false),
    COMPACT_COLON(":", true, true),
    CASE_COLON(":", true, false),
    COLON(":"),
    IDENTIFIER("identifier"),
    OPEN_CURLY_BRACE("{", false, false, false, true),
    CLOSING_CURLY_BRACE("}", false, false, true, false),
    COMPACT_OPEN_SQUARE_BRACKET("[", true, true),
    COMPACT_CLOSING_SQUARE_BRACKET("]", true, true),
    OPEN_SQUARE_BRACKET("[", false, true),
    CLOSING_SQUARE_BRACKET("]", true, false),
    EQUAL("="),
    COMPACT_OPEN_PARENTHESIS("(", true, true),
    OPEN_PARENTHESIS("(", false, true),
    CLOSING_PARENTHESIS(")", true, false),
    FOR("for"),
    IF("if"),
    WHILE("while"),
    SWITCH("switch"),
    IN("in"),
    LET("let"),
    CONST("const"),
    DO("do"),
    CASE("case"),
    SEMI(";", true, false, false, true),
    CATCH("catch"),
    DOT(".", true, true),
    VERTICAL_BAR("|"),
    COMPACT_VERTICAL_BAR("|", true, true),

    FINALLY("finally"),
    RETURN("return"),
    THROW("throw"),
    CONTINUE("continue"),
    BREAK("break"),
    YIELD("yield"),
    ASSERT("assert"),
    UNDEFINED("undefined"),
    NULL("NULL"),

    ARROW("->"),
    TS_ARRAY("=>"),
    AND("&&"),
    ABSTRACT("abstract"),
    PRIVATE("private"),
    PROTECTED("protected"),
    STATIC("static"),
    VOID("void"),
    THIS("this"),
    TRIPLE_DOTS("...", true, true),
    NOT("!", false, true),
    BIT_INVERSE("~", false, true),
    PLUS_EQUAL("+=", false, true),
    MINUS_EQUAL("-=", false, true),
    PREFIX_PLUS_PLUS("++", false,true,false,false, LexiconType.PREFIX),
    PREFIX_MINUS_MINUS("--", false,true,false,false, LexiconType.PREFIX),
    POSTFIX_PLUS_PLUS("++", true,false,false,false, LexiconType.POSTFIX),
    POSTFIX_MINUS_MINUS("--", true,false,false,false, LexiconType.POSTFIX),
    NEW("new"),
    INSTANCEOF("instanceof"),
    SUPER("super"),
    AS("as"),
    BIT_AND("&"),
    CLASS("class"),
    EXTENDS("extends"),
    QUESTION("?"),
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT(">>"),
    RIGHT_SHIFT_UNSIGNED(">>>"),
    READONLY("readonly"),
    CONSTRUCTOR("constructor"),
    IMPORT("import"),
    FROM("from"),
    ELSE("else");


    private enum LexiconType {
        PREFIX, POSTFIX, OTHER
    }


    private final String text;
    private final boolean noPrecedingWhiteSpace;
    private final boolean noSucceedingWhiteSpace;
    private final boolean newLineBefore;
    private final boolean newLineAfter;
    private final LexiconType type;

    TsLexicon(String text) {
        this(text, false, false);
    }

    TsLexicon(String text,
              boolean noPrecedingWhiteSpace,
              boolean noSucceedingWhiteSpace) {
        this(text, noPrecedingWhiteSpace, noSucceedingWhiteSpace, false, false);
    }

    TsLexicon(String text,
              boolean noPrecedingWhiteSpace,
              boolean noSucceedingWhiteSpace,
              boolean newLineBefore,
              boolean newLineAfter) {
        this(text, noPrecedingWhiteSpace, noSucceedingWhiteSpace, newLineBefore, newLineAfter, LexiconType.OTHER);
    }

    TsLexicon(String text,
              boolean noPrecedingWhiteSpace,
              boolean noSucceedingWhiteSpace,
              boolean newLineBefore,
              boolean newLineAfter,
              LexiconType type
                      ) {
        this.text = text;
        this.noPrecedingWhiteSpace = noPrecedingWhiteSpace;
        this.noSucceedingWhiteSpace = noSucceedingWhiteSpace;
        this.newLineBefore = newLineBefore;
        this.newLineAfter = newLineAfter;
        this.type = type;
    }

    public String text() {
        return text;
    }

    public boolean noPrecedingWhiteSpace() {
        return noPrecedingWhiteSpace;
    }

    public boolean noSucceedingWhiteSpace() {
        return noSucceedingWhiteSpace;
    }

    public boolean newLineBefore() {
        return newLineBefore;
    }

    public boolean newLineAfter() {
        return newLineAfter;
    }

    public static TsLexicon getByTextExcludingPostfix(String text) {
        return NncUtils.findRequired(values(), v -> v.text.equals(text) && v.type != LexiconType.POSTFIX);
    }

    public static TsLexicon getByTextExcludingPrefix(String text) {
        return NncUtils.findRequired(values(), v -> v.text.equals(text) && v.type != LexiconType.PREFIX);
    }

    public static TsLexicon getByText(String text) {
        return NncUtils.findRequired(values(), v -> v.text.equals(text));
    }

}
