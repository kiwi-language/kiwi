package org.metavm.compiler.file;

import junit.framework.TestCase;

import java.nio.CharBuffer;

public class SourceFileTest extends TestCase {

    public void testComputePos() {
        var text = """
        Hello,
        Kiwi
        """;
        var pos = SourceFile.computePos(CharBuffer.wrap(text.toCharArray()), 11);
        assertEquals(2, pos.line());
        assertEquals(5, pos.column());
    }

    public void testGetLine() {
        var text = """
                Hello,
                Kiwi
                """;
        var cb = CharBuffer.wrap(text.toCharArray());
        var line1 = new SourceLine("Hello,", 0);
        for (int i = 0; i < 7; i++) {
            assertEquals(line1, SourceFile.getLine(cb, i));
        }
        var line2 = new SourceLine("Kiwi", 7);
        for (int i = 7; i < 11; i++) {
            assertEquals(line2, SourceFile.getLine(cb, i));
        }
        assertNull(SourceFile.getLine(cb, 12));
    }

}