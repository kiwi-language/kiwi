package org.metavm.compiler.syntax;

import junit.framework.TestCase;
import org.metavm.compiler.diag.DefaultLog;
import org.metavm.compiler.diag.DiagFactory;
import org.metavm.compiler.file.DummySourceFile;

import java.io.PrintWriter;

public class LexerTest extends TestCase {

    public void testLA() {
        var lexer = lexer("priv fn test() {}");
        var la = lexer.la(0);
        assertSame(TokenKind.PRIV, la.get().getKind());
        assertSame(TokenKind.FN, la.peek().getKind());
        assertTrue(la.hasNext());
        la.next();
        assertSame(TokenKind.PRIV, la.prev().getKind());
        assertSame(TokenKind.FN, la.get().getKind());
        assertSame(TokenKind.IDENT, la.peek().getKind());
        for (int i = 0; i < 6; i++) {
            la.next();
        }
        assertSame(TokenKind.EOF, la.get().getKind());
        assertFalse(la.hasNext());
    }

    private Lexer lexer(String text) {
        var log = new DefaultLog(new DummySourceFile(text), DiagFactory.instance,
                new PrintWriter(System.out),
                new PrintWriter(System.err));
        return new Lexer(log, text.toCharArray(), text.length());
    }

}