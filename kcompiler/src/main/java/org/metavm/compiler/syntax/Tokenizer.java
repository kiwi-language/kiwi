package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.metavm.compiler.syntax.Unicodes.EOI;

public class Tokenizer {

    public static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);

    private final Log log;
    private final UnicodeReader reader;
    private int line;

    private int[] lineStarts = new int[8];

    private final StringBuilder buf = new StringBuilder(256);

    public Tokenizer(Log log, char[] buf, int len) {
        this.log = log;
        reader = new UnicodeReader(buf, len);
    }

    public Token nextToken() {
        var start = pos();
        return switch (get()) {
            case ' ', '\f', '\t' -> {
                skipWhitespaces();
                yield nextToken();
            }
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '$', '_'
                 -> nextKeywordOrIdent(true);
            case '\"' -> nextStringLit();
            case '\'' -> nextCharLit();
            case '0' -> {
                next();
                if (accept('x'))
                    yield nextNumberLit(start, 16);
                else if (accept('b'))
                    yield nextNumberLit(start, 2);
                else if (accept('o'))
                    yield nextNumberLit(start, 8);
                else {
                    put('0');
                    yield nextNumberLit(start, 10);
                }
            }
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> nextNumberLit(start, 10);
            case '+' -> {
                next();
                yield  switch (get()) {
                    case '+' -> {
                        next();
                        yield new Token(TokenKind.INC, start, pos());
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
                    case '>' -> {
                        next();
                        yield new Token(TokenKind.ARROW, start, pos());
                    }
                    case '-' -> {
                        next();
                        yield new Token(TokenKind.DEC, start, pos());
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
                        next();
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
                yield switch (get()) {
                    case '=' -> {
                        next();
                        yield new Token(TokenKind.NE, start, pos());
                    }
                    case '!' -> {
                        next();
                        yield new Token(TokenKind.NONNULL, start, pos());
                    }
                    default -> new Token(TokenKind.NOT, start, pos());
                };
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
                        if (accept('>')) {
                            if (accept('='))
                                yield new Token(TokenKind.USHR_ASSIGN, start, pos());
                            else
                                yield new Token(TokenKind.USHR, start, pos());
                        } else if (accept('='))
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
            case ',' -> {
                next();
                yield new Token(TokenKind.COMMA, start, pos());
            }
            case '.' -> {
                next();
                if (is('.')) {
                    next();
                    if (is('.')) {
                        next();
                        yield new Token(TokenKind.ELLIPSIS, start, pos());
                    }
                    else {
                        log.error(pos() - 1, Errors.illegal(TokenKind.DOT));
                        yield new Token(TokenKind.ERROR, start, pos());
                    }
                }
                else
                    yield new Token(TokenKind.DOT, start, pos());
            }
            case '?' -> {
                next();
                yield new Token(TokenKind.QUES, start, pos());
            }
            case '@' -> {
                next();
                yield new Token(TokenKind.AT, start, pos());
            }
            case ';' -> {
                next();
                yield new Token(TokenKind.SEMICOLON, start, pos());
            }
            case ':' -> {
                next();
                yield new Token(TokenKind.COLON, start, pos());
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
            case EOI -> Tokens.EOF;
            default -> {
                if (!isASIii() && Character.isJavaIdentifierPart(get()))
                    yield nextKeywordOrIdent(true);
                else {
                    log.error(pos(), Errors.unexpectedChar(get()));
                    next();
                    yield nextToken();
                }
            }
        };
    }

    private void processLineTerminator() {
        line++;
        if (line >= lineStarts.length)
            lineStarts = Arrays.copyOf(lineStarts, lineStarts.length * 2);
        lineStarts[line] = pos();
    }

    public int getLine(int pos) {
        var ln = Arrays.binarySearch(lineStarts, 0, line + 1, pos);
        return ln < 0 ? -ln - 2 : ln;
    }

    private Token nextNumberLit(int start, int radix) {
        var error = false;
        var isFloat = false;
        scanDigits(radix);
        TokenKind tk;
        if (radix == 10) {
            if (is('.')) {
                next();
                if (is('.'))
                    reader.reset(pos() - 1);
                else {
                    isFloat = true;
                    put('.');
                    scanDigits(radix);
                }
            } else if (is('e')) {
                isFloat = true;
                putAndNext();
                if (isDigit(10))
                    scanDigits(10);
                else {
                    log.error(start, Errors.MALFORMED_FLOAT_LITERAL);
                    error = true;
                }
            }
        } else if (radix == 16) {
            if (is('p')) {
                isFloat = true;
                putAndNext();
                if (isDigit(10))
                    scanDigits(10);
                else {
                    log.error(start, Errors.MALFORMED_FLOAT_LITERAL);
                    error = true;
                }
            }
        }
        if (acceptOneOf('d', 'D'))
            tk = TokenKind.DOUBLE_LIT;
        else if (acceptOneOf('f', 'F'))
            tk = TokenKind.FLOAT_LIT;
        else if (!isFloat && acceptOneOf('l', 'L'))
            tk = TokenKind.LONG_LIT;
        else
            tk = isFloat ? TokenKind.DOUBLE_LIT : TokenKind.INTEGER_LIT;
        if (error)
            return new Token(TokenKind.ERROR, start, pos());
        else
            return new NumberToken(tk, start, pos(), takeBuffered(), radix);
    }

    private boolean scanDigits(int radix) {
        if (isDigit(radix)) {
            do {
                putAndNext();
            } while (isDigit(radix));
            return true;
        }
        else
            return false;
    }

    private Token nextCharLit() {
        var start = pos();
        next();
        var c = scanChar(false);
        if (accept('\'') && c != -1)
            return new NumberToken(TokenKind.CHAR_LIT, start, pos(), Integer.toString(c), 10);
        else
            return new Token(TokenKind.ERROR, start, pos());
    }

    private Token nextStringLit() {
        var start = pos();
        next();
        var error = false;
        var isTextBlock = accept("\"\"");
        if (isTextBlock) {
            while (!accept("\"\"\"")) {
                var c = scanChar(true);
                if (c == -1)
                    error = true;
                else if (!error)
                    put(c);
            }
        }
        else {
            while (!accept('\"')) {
                var c = scanChar(false);
                if (c == -1)
                    error = true;
                else if (!error)
                    put(c);
            }
        }
        if (error) {
            clearBuffer();
            return new Token(TokenKind.ERROR, start, pos());
        }
        else {
            var text = isTextBlock ? takeBuffered().stripIndent() : takeBuffered();
            return new StringToken(TokenKind.STRING_LIT, start, pos(), text);
        }
    }

    private int scanChar(boolean isTextBlock) {
        var c = getAndNext();
        if (c == '\\') {
            return switch (get()) {
                case 'u' -> {
                    next();
                    yield scanUnicodeEscape();
                }
                case 'n' -> {
                    next();
                    yield '\n';
                }
                case 's' -> {
                    next();
                    yield ' ';
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
                case '\\' -> {
                    next();
                    yield '\\';
                }
                case '\'' -> {
                    next();
                    yield '\'';
                }
                case '\n' -> {
                    next();
                    processLineTerminator();
                    if (isTextBlock)
                        yield '\n';
                    else {
                        log.error(pos(), Errors.illegalEscChar);
                        yield -1;
                    }
                }
                case '\r' -> {
                    next();
                    accept('\n');
                    processLineTerminator();
                    if (isTextBlock)
                        yield '\n';
                    else {
                        log.error(pos(), Errors.illegalEscChar);
                        yield -1;
                    }
                }
                case '0', '1', '2', '3', '4', '5', '6', '7' -> scanOctalEscapeSeq();
                default -> {
                    log.error(pos(), Errors.illegalEscChar);
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
        if (isDigit(8)) {
            code = (code << 3) + (getAndNext() - '0');
            if (isDigit(8))
                code = (code << 3) + (getAndNext() - '0');
        }
        return (char) code;
    }

    private boolean isDigit(int radix) {
        return digit(radix) != -1;
    }

    private int digit(int radix) {
        var c = get();
        var code = switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> c - '0';
            case 'A', 'B', 'C', 'D', 'E', 'F' -> c - 'A' + 10;
            case 'a', 'b', 'c', 'd', 'e', 'f' -> c - 'a' + 10;
            default -> {
                if (isASIii())
                    yield Character.digit(c, radix);
                else
                    yield -1;
            }
        };
        return code <= radix ? code : -1;
    }

    private int scanUnicodeEscape() {
        var start = pos();
        int code = 0;
        int i = 0;
        for (; i < 4; i++) {
            int d;
            if ((d = digit(16)) != -1)
                code = code << 4 | d;
            else {
                code = -1;
                break;
            }
            next();
        }
        if (code  == -1)
            log.error(start, Errors.invalidUnicodeEscape);
        return code;
    }

    private int getAndNext() {
        var c = get();
        next();
        return c;
    }

    private boolean acceptOneOf(int ch1, int ch2) {
        return accept(ch1) || accept(ch2);
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
        while (isOneOf(' ', '\t', '\f'))
            next();
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
                     'v', 'w', 'x', 'y', 'z', '$' , '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
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
            if (tk == TokenKind.IDENT)
                return new NamedToken(TokenKind.IDENT, start, pos(), name);
            else
                return new Token(tk, start, pos());
        }
        else
            return new NamedToken(TokenKind.IDENT, start, pos(), name);
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
