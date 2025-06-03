package org.metavm.compiler.diag;

import junit.framework.TestCase;
import org.metavm.compiler.file.SourceFile;
import org.metavm.compiler.file.SourcePos;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

public class LogTest extends TestCase {

    public void test() {
        var bout = new ByteArrayOutputStream();
        var out = new PrintWriter(bout);
        var errBout = new ByteArrayOutputStream();
        var err = new PrintWriter(errBout);

        var symNotFound = "Symbol ''{0}'' not found";
        var log = new Log(new MockSourceFile(),
                new DiagFactory(new MockFmt(
                        Map.of(
                                DiagCode.SYMBOL_NOT_FOUND.getKey(),
                                symNotFound
                        )
                )),
                out, err);
        log.error(new MockDiagPos(31), new Error(DiagCode.SYMBOL_NOT_FOUND, "name"));
        log.flush();
        assertEquals("Symbol 'name' not found", errBout.toString());
    }

}

class MockFmt implements DiagFmt {

    private final Map<String, String> patterns;

    MockFmt(Map<String, String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public String format(Diag diag, Locale l) {
        var ptn = patterns.get(diag.getCode().getKey());
        return MessageFormat.format(ptn, diag.getArgs());
    }
}

record MockDiagPos(int pos) implements DiagPos {

    @Override
    public int getIntPos() {
        return pos;
    }
}

class MockSourceFile implements SourceFile {

    @Override
    public CharBuffer getContent() {
        return CharBuffer.allocate(0);
    }

    @Override
    public SourcePos computePos(int pos) {
        return new SourcePos(pos / 10, pos % 10);
    }
}