package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Name;

import static org.metavm.compiler.syntax.Unicodes.EOI;

public class Lexer {
    private final Log log;
    private final UnicodeReader reader;
    private final StringBuilder buf = new StringBuilder(256);

    public Lexer(Log log, char[] buf) {
        this.log = log;
        reader = new UnicodeReader(buf);
    }

    private Token nextToken() {
        var start = pos();
        return switch (get()) {
            case ' ', '\f', '\t' -> {
                skipWhitespaces();
                yield nextToken();
            }
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'y', 'z', '$', '_'
                 -> nextKeywordOrIdent(true);
            case '\"' -> nextStringLit();
            case '\'' -> nextCharLit();
            case '0' -> {
                if (accept('x'))
            }
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> nextNumberLit(10);
            case '+' -> {
                next();
                yield  switch (get()) {
                    case '+' -> {
                        next();
                        yield new Token(TokenKind.PLUS_PLUS, start, pos());
                    }
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.PLUS_ASSIGN, start, pos());
                    }
                    default -> new Token(TokenKind.PLUS, start, pos());
                };
            }
            case '-' -> {
                next();
                yield switch (get()) {
                    case '-' -> {
                        next();
                        yield new Token(TokenKind.MINUS_MINUS, start, pos());
                    }
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.MINUS_ASSIGN, start, pos());
                    }
                    default -> new Token(TokenKind.MINUS, start, pos());
                };
            }
            case '*' -> {
                next();
                if (accept('='))
                    yield new Token(TokenKind.MUL_ASSIGN, start, pos());
                else
                    yield new Token(TokenKind.MUL, start, pos());
            }
            case '/' -> {
                next();
                if (accept('/')) {
                    skipToEoln();
                    yield nextToken();
                }
                if (accept('*')) {
                    while (!isEof()) {
                        if (accept('*') && accept('/'))
                            yield nextToken();
                    }
                    log.error(pos(), Errors.UNCLOSED_COMMENT);
                    yield Tokens.EOF;
                }
                if (accept('='))
                    yield new Token(TokenKind.DIV_ASSIGN, start, pos());
                else
                    yield new Token(TokenKind.DIV, start, pos());
            }
            case '%' -> {
                next();
                if (accept('='))
                    yield new Token(TokenKind.MOD_ASSIGN, start, pos());
                else
                    yield new Token(TokenKind.MOD, start, pos());
            }
            case '=' -> {
                next();
                if (accept('='))
                    yield new Token(TokenKind.EQ, start, pos());
                 else
                    yield new Token(TokenKind.ASSIGN, start, pos());
            }
            case '!' -> {
                next();
                if (accept('='))
                    yield new Token(TokenKind.NE, start, pos());
                else
                    yield new Token(TokenKind.NOT, start, pos());
            }
            case '<' -> {
                next();
                yield switch (get()) {
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.LE, start, pos());
                    }
                    case '<' -> {
                        next();
                        if (accept('='))
                            yield new Token(TokenKind.SHL_ASSIGN, start, pos());
                        else
                            yield new Token(TokenKind.SHL, start, pos());
                    }
                    default -> new Token(TokenKind.LT, start, pos());
                };
            }
            case '>' -> {
                next();
                yield switch (get()) {
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.GE, start, pos());
                    }
                    case '>' -> {
                        next();
                        if (accept('>'))
                            yield new Token(TokenKind.USHR_ASSIGN, start, pos());
                        else if (accept('='))
                            yield new Token(TokenKind.SHR_ASSIGN, start, pos());
                        else
                            yield new Token(TokenKind.SHR, start, pos());
                    }
                    default -> new Token(TokenKind.GT, start, pos());
                };
            }
            case '&' -> {
                next();
                yield switch (get()) {
                    case '&' -> {
                        next();
                        if (accept('='))
                            yield new Token(TokenKind.AND_ASSIGN, start, pos());
                        else
                            yield new Token(TokenKind.AND, start, pos());
                    }
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.BITAND_ASSIGN, start, pos());
                    }
                    default -> new Token(TokenKind.BITAND, start, pos());
                };
            }
            case '|' -> {
                next();
                yield switch (get()) {
                    case '|' -> {
                        next();
                        if (accept('='))
                            yield new Token(TokenKind.OR_ASSIGN, start, pos());
                        else
                            yield new Token(TokenKind.OR, start, pos());
                    }
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.BITOR_ASSIGN, start, pos());
                    }
                    default -> new Token(TokenKind.BITOR, start, pos());
                };
            }
            case '^' -> {
                next();
                if (accept('='))
                    yield new Token(TokenKind.BITXOR_ASSIGN, start, pos());
                else
                    yield new Token(TokenKind.BITXOR, start, pos());
            }
            case '~' -> {
                next();
                yield new Token(TokenKind.BITNOT, start, pos());
            }
            case '(' -> {
                next();
                yield new Token(TokenKind.LPAREN, start, pos());
            }
            case ')' -> {
                next();
                yield new Token(TokenKind.RPAREN, start, pos());
            }
            case '[' -> {
                next();
                yield new Token(TokenKind.LBRACKET, start, pos());
            }
            case ']' -> {
                next();
                yield new Token(TokenKind.RBRACKET, start, pos());
            }
            case '{' -> {
                next();
                yield new Token(TokenKind.LBRACE, start, pos());
            }
            case '}' -> {
                next();
                yield new Token(TokenKind.RBRACE, start, pos());
            }
            case '\n' -> {
                next();
                processLineTerminator();
                yield nextToken();
            }
            case '\r' -> {
                next();
                accept('\n');
                processLineTerminator();
                yield nextToken();
            }
            case EOI -> {
                next();
                yield Tokens.EOF;
            }
            default -> {
                if (!isASIii() && Character.isJavaIdentifierPart(get()))
                    yield nextKeywordOrIdent(true);
                else {
                    log.error(start, Errors.unexpectedChar(Character.toString(get())));
                    yield nextToken();
                }
            }
        };
    }

    private Token nextNumberLit(int radix) {
        var start = pos();
        putAndNext();
        while (isDigit()) {
            putAndNext();
        }
        TokenKind tk;
        if (accept('.')) {
            while (isDigit())
                putAndNext();
            if (accept('f') || accept('F'))
                tk = TokenKind.FLOAT_LIT;
            else {
                if (!accept('d'))
                    accept('D');
                tk = TokenKind.DOUBLE_LIT;
            }
        }
        else {
            if (accept('l') || accept('L'))
                tk = TokenKind.LONG_LIT;
            else
                tk = TokenKind.INTEGER_LIT;
        }
        return new NumberToken(tk, start, pos(), takeBuffered(), radix);
    }

    private Token nextCharLit() {
        var start = pos();
        next();
        var c = scanChar();
        if (accept('\'') && c != -1)
            return new NumberToken(TokenKind.CHAR_LIT, start, pos(), Integer.toString(c), 10);
        else
            return new Token(TokenKind.ERROR, start, pos());
    }

    private Token nextStringLit() {
        var start = pos();
        next();
        var error = false;
        while (!accept('\"')) {
            var c = scanChar();
            if (c == -1)
                error = true;
            else if (!error)
                put(c);
        }
        if (error) {
            clearBuffer();
            return new Token(TokenKind.ERROR, start, pos());
        }
        else
            return new StringToken(TokenKind.STRING_LIT, start, pos(), takeBuffered());
    }

    private int scanChar() {
        var c = getAndNext();
        if (c == '\\') {
            next();
            return switch (get()) {
                case 'u' -> {
                    next();
                    yield scanUnicodeEscape();
                }
                case 'n' -> {
                    next();
                    yield '\n';
                }
                case 'f' -> {
                    next();
                    yield '\f';
                }
                case 't' -> {
                    next();
                    yield '\t';
                }
                case 'r' -> {
                    next();
                    yield '\r';
                }
                case 'b' -> {
                    next();
                    yield '\b';
                }
                case '\"' -> {
                    next();
                    yield '\"';
                }
                case '0', '1', '2', '3', '4', '5', '6', '7' -> scanOctalEscapeSeq();
                default -> {
                    log.error(pos(), Errors.invalidEscape(Character.toString(get())));
                    next();
                    yield -1;
                }
            };
        }
        else
            return (char) c;
    }

    private char scanOctalEscapeSeq() {
        var code = getAndNext() - '0';
        if (isDigit()) {
            code = (code << 3) + (getAndNext() - '0');
            if (isDigit())
                code = (code << 3) + (getAndNext() - '0');
        }
        return (char) code;
    }

    private boolean isDigit() {
        return switch (get()) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }

    private char scanUnicodeEscape() {

    }

    private int getAndNext() {
        var c = get();
        next();
        return c;
    }

    private void processLineTerminator() {

    }

    private boolean accept(int ch) {
        if (is(ch)) {
            next();
            return true;
        }
        else
            return false;
    }

    private boolean accept(String s) {
        var savedPos = pos();
        var len = s.length();
        for (int i = 0; i < len; i++) {
            var c = s.charAt(i);
            if (!accept(c)) {
                reader.reset(savedPos);
                return false;
            }
        }
        return true;
    }

    private void skipToEoln() {
        while (!isEof() && !isEoln())
            next();
    }

    private boolean isEoln() {
        return isOneOf('\r', '\n');
    }

    private boolean is(int ch) {
        return get() == ch;
    }

    private boolean isOneOf(int ch1, int ch2) {
        return is(ch1) || is(ch2);
    }

    private boolean isOneOf(int ch1, int ch2, int ch3) {
        return is(ch1) || is(ch2) || is(ch3);
    }

    private void skipWhitespaces() {

    }

    private Token nextKeywordOrIdent(boolean checkKeyword) {
        var start = pos();
        putAndNext();
        out: for (;;) {
            var c = get();
            switch (c) {
                case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                     'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                     'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
                     'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
                     'v', 'w' , '$' , '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                        -> putAndNext();
                case '\u0000', '\u0001', '\u0002', '\u0003',
                        '\u0004', '\u0005', '\u0006', '\u0007',
                        '\u0008', '\u000E', '\u000F', '\u0010',
                        '\u0011', '\u0012', '\u0013', '\u0014',
                        '\u0015', '\u0016', '\u0017',
                        '\u0018', '\u0019', '\u001B',
                        '\u007F'->
                    next();
                default -> {
                    // All valid ASCii characters have been processed
                    if (!isASIii() && Character.isJavaIdentifierPart(c))
                        putAndNext();
                    else
                        break out;
                }
            }
        }
        var name = Name.from(takeBuffered());
        if (checkKeyword) {
            var tk = Tokens.lookupKind(name);
            if (tk == TokenKind.IDENTIFIER)
                return new NamedToken(TokenKind.IDENTIFIER, start, pos(), name);
            else
                return new Token(tk, start, pos());
        }
        else
            return new NamedToken(TokenKind.IDENTIFIER, start, pos(), name);
    }


    private String takeBuffered() {
        var s = buf.toString();
        buf.setLength(0);
        return s;
    }

    private void clearBuffer() {
        buf.setLength(0);
    }

    private boolean isASIii() {
        return get() < 0x7f;
    }

    private void putAndNext() {
        put();
        next();
    }

    private void put() {
        put(get());
    }

    private void put(int code) {
        buf.appendCodePoint(code);
    }

    private int get() {
        return reader.get();
    }

    private int pos() {
        return reader.pos();
    }

    private void next() {
        reader.next();
    }

    private boolean isEof() {
        return reader.get() == EOI;
    }

}
