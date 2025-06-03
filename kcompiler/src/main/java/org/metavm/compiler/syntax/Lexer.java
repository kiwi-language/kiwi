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
        if (isEof())
            return Tokens.EOF;
        var pos = pos();
        return switch (get()) {
            case ' ', '\f', '\t' -> {
                skipWhitespaces();
                yield nextToken();
            }
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'y', 'z', '$', '_'
                 // Add more cases for other characters as needed
                 -> nextKeywordOrIdent();
            case '+' -> {
                next();
                if (!isEof()) {
                    yield  switch (get()) {
                        case '+' -> {
                            next();
                            yield new Token(TokenKind.PLUS_PLUS, pos, pos());
                        }
                        case '=' -> {
                            next();
                            yield new Token(TokenKind.PLUS_ASSIGN, pos, pos());
                        }
                        default -> new Token(TokenKind.PLUS, pos, pos());
                    };
                }
                else
                    yield new Token(TokenKind.PLUS, pos, pos());
            }
            case '-' -> {
                next();
                if (!isEof()) {
                    yield switch (get()) {
                        case '-' -> {
                            next();
                            yield new Token(TokenKind.MINUS_MINUS, pos, pos());
                        }
                        case '=' -> {
                            next();
                            yield new Token(TokenKind.MINUS_ASSIGN, pos, pos());
                        }
                        default -> new Token(TokenKind.MINUS, pos, pos());
                    };
                }
                else
                    yield new Token(TokenKind.MINUS, pos, pos());
            }
            case '*' -> {
                next();
                if (!isEof() && get() == '=') {
                    next();
                    yield new Token(TokenKind.MUL_ASSIGN, pos, pos());
                } else
                    yield new Token(TokenKind.MUL, pos, pos());
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
                if (!isEof() && get() == '=') {
                    next();
                    yield new Token(TokenKind.DIV_ASSIGN, pos, pos());
                } else
                    yield new Token(TokenKind.DIV, pos, pos());
            }
            case '%' -> {
                next();
                if (!isEof() && get() == '=') {
                    next();
                    yield new Token(TokenKind.MOD_ASSIGN, pos, pos());
                } else
                    yield new Token(TokenKind.MOD, pos, pos());
            }
            default -> {
                log.error(pos, Errors.unexpectedChar(Character.toString(get())));
                yield nextToken();
            }
        };
    }

    private boolean accept(int ch) {
        if (is(ch)) {
            next();
            return true;
        }
        else
            return false;
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

    private Token nextKeywordOrIdent() {
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
        var tk = Tokens.lookupKind(name);
        if (tk == TokenKind.IDENTIFIER)
            return new NamedToken(TokenKind.IDENTIFIER, start, pos(), name);
        else
            return new Token(tk, start, pos());
    }


    private String takeBuffered() {
        var s = buf.toString();
        buf.setLength(0);
        return s;
    }

    private boolean isASIii() {
        return get() < 0x7f;
    }

    private void putAndNext() {
        put();
        next();
    }

    private void put() {
        buf.appendCodePoint(get());
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
