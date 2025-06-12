package org.metavm.compiler.diag;

import junit.framework.TestCase;
import org.metavm.compiler.file.DummySourceFile;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
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
        var log = new DefaultLog(new DummySourceFile("name"),
                new DiagFactory(new MockFmt(
                        Map.of(
                                "symbol.not.found",
                                symNotFound
                        )
                )),
                out, err);
        log.error(new MockDiagPos(0), new Error("symbol.not.found", "name"));
        log.flush();
        assertEquals("dummy.kiwi:1: Symbol 'name' not found\n", errBout.toString());
    }


    class MockFmt implements DiagFmt {

        private final Map<String, String> patterns;

        MockFmt(Map<String, String> patterns) {
            this.patterns = patterns;
        }

        @Override
        public String format(Diag diag, Locale l) {
            var ptn = patterns.get(diag.getCode());
            return MessageFormat.format(ptn, diag.getArgs());
        }
    }

    record MockDiagPos(int pos) implements DiagPos {

        @Override
        public int getIntPos() {
            return pos;
        }
    }


}