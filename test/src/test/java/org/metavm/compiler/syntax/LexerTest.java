package org.metavm.compiler.syntax;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.diag.Error;
import org.metavm.compiler.diag.*;

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
        log.error(error.code().name());
    }

    @Override
    public void error(DiagPos pos, Error error) {
        log.error(error.code().name());
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