package org.metavm.compiler.syntax;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.diag.Error;
import org.metavm.compiler.diag.*;
import org.metavm.compiler.element.Name;

import java.util.Map;

public class LexerTest extends TestCase {

    public void testString() {
        assertEquals(
                new StringToken(
                        TokenKind.STRING_LIT,
                        0,
                        6,
                        "Kiwi"
                ),
                token("\"Kiwi\"")
        );
    }

    public void testUnicodeEscape() {
        assertEquals(
                new StringToken(
                        TokenKind.STRING_LIT,
                        0,
                        8,
                        "\u0001"
                ),
                token("\"\\u0001\"")
        );
    }

    public void testOctalEscapeSeq() {
        assertEquals(
                new StringToken(
                        TokenKind.STRING_LIT,
                        0,
                        6,
                        "\001"
                ),
                token("\"\\001\"")
        );
    }

    public void testTextBlock() {
        assertEquals(
                new StringToken(
                        TokenKind.STRING_LIT,
                        0,
                        28,
                        "\nHello,\nKiwi\n"
                ),
                token("\"\"\"\n   Hello,\n   Kiwi\n   \"\"\"")
        );
    }

    public void testOperators() {
        var map = Map.ofEntries(
                Map.entry(TokenKind.ASSIGN, "="),
                Map.entry(TokenKind.EQ, "=="),
                Map.entry(TokenKind.BITOR, "|"),
                Map.entry(TokenKind.OR, "||"),
                Map.entry(TokenKind.BITOR_ASSIGN, "|="),
                Map.entry(TokenKind.OR_ASSIGN, "||="),
                Map.entry(TokenKind.NOT, "!"),
                Map.entry(TokenKind.BITNOT, "~"),
                Map.entry(TokenKind.BITAND, "&"),
                Map.entry(TokenKind.AND, "&&"),
                Map.entry(TokenKind.BITAND_ASSIGN, "&="),
                Map.entry(TokenKind.AND_ASSIGN, "&&="),
                Map.entry(TokenKind.BITXOR, "^"),
                Map.entry(TokenKind.BITXOR_ASSIGN, "^="),
                Map.entry(TokenKind.PLUS, "+"),
                Map.entry(TokenKind.PLUS_ASSIGN, "+="),
                Map.entry(TokenKind.INC, "++"),
                Map.entry(TokenKind.MINUS, "-"),
                Map.entry(TokenKind.MINUS_ASSIGN, "-="),
                Map.entry(TokenKind.DEC, "--"),
                Map.entry(TokenKind.MUL, "*"),
                Map.entry(TokenKind.MUL_ASSIGN, "*="),
                Map.entry(TokenKind.DIV, "/"),
                Map.entry(TokenKind.DIV_ASSIGN, "/="),
                Map.entry(TokenKind.MOD, "%"),
                Map.entry(TokenKind.MOD_ASSIGN, "%="),
                Map.entry(TokenKind.LT, "<"),
                Map.entry(TokenKind.LE, "<="),
                Map.entry(TokenKind.SHL, "<<"),
                Map.entry(TokenKind.SHL_ASSIGN, "<<="),
                Map.entry(TokenKind.GT, ">"),
                Map.entry(TokenKind.GE, ">="),
                Map.entry(TokenKind.SHR, ">>"),
                Map.entry(TokenKind.USHR, ">>>"),
                Map.entry(TokenKind.SHR_ASSIGN, ">>="),
                Map.entry(TokenKind.USHR_ASSIGN, ">>>=")
        );
        map.forEach((tk, text) -> {
            assertEquals(
                    new Token(tk, 0, text.length()),
                    token(text)
            );
        });
    }

    public void testNumericToken() {
        var lexer = new Lexer(
                new MockLog(),
                "0.1f".toCharArray()
        );
        var t = lexer.nextToken();
        assertEquals(
                new NumberToken(
                        TokenKind.FLOAT_LIT,
                        0,
                        4,
                        "0.1",
                        10
                ),
                t
        );
    }

    public void testHexNumber() {
        assertEquals(
                new NumberToken(
                        TokenKind.INTEGER_LIT,
                        0,
                        4,
                        "1A",
                        16
                ),
                token("0x1A")
        );

        assertEquals(
                new NumberToken(
                        TokenKind.DOUBLE_LIT,
                        0,
                        6,
                        "1Ap2",
                        16
                ),
                token("0x1Ap2")
        );
    }

    public void testBinaryNumber() {
        assertEquals(
                new NumberToken(
                        TokenKind.INTEGER_LIT,
                        0,
                        4,
                        "10",
                        2
                ),
                token("0b10")
        );
    }

    public void testOctalNumber() {
        assertEquals(
                new NumberToken(
                        TokenKind.INTEGER_LIT,
                        0,
                        4,
                        "17",
                        8
                ),
                token("0o17")
        );
    }

    public void testWhitespace() {
        assertEquals(
                new NumberToken(
                        TokenKind.INTEGER_LIT,
                        7,
                        8,
                        "1",
                        10
                ),
                token(" \t\f\n\r\n\r1")
        );
    }

    public void testKeyword() {
        for (TokenKind tk : TokenKind.values()) {
            if (tk.isKeyword()) {
                assertEquals(
                        new Token(tk, 0, tk.name().length()),
                        token(tk.name().toLowerCase())
                );
            }
        }
    }

    public void testIdent() {
        assertEquals(
                new NamedToken(TokenKind.IDENT, 0, 4, Name.from("name")),
                token("name")
        );
    }

    private Token token(String text) {
        return lexer(text).nextToken();
    }

    private Lexer lexer(String text) {
        return new Lexer(new MockLog(), text.toCharArray());
    }

}

@Slf4j
class MockLog implements Log {

    @Override
    public void error(int pos, Error error) {
        log.error(error.code());
    }

    @Override
    public void error(DiagPos pos, Error error) {
        log.error(error.code());
    }

    @Override
    public void warn(DiagPos pos, Warning warning) {

    }

    @Override
    public void note(DiagPos pos, Note note) {

    }

    @Override
    public void flush() {

    }
}