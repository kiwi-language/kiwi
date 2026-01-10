package org.manul.compiler.diag;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.manul.compiler.file.SourceFile;
import org.manul.compiler.syntax.TokenKind;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.CharBuffer;

@Slf4j
public class DefaultLogTest extends TestCase {


    public void test() {
        var source = """
            class Product(
                var name: string
                var price: double
            )
            """;
        var bout = new ByteArrayOutputStream();
        var file = new MockSourceFile(source);
        var l = new DefaultLog(file, DiagFactory.instance,
                new PrintWriter(bout),
                new PrintWriter(bout)
        );
        var pos = source.indexOf("string") + "string".length();
        l.error(pos, Errors.expected(TokenKind.COMMA));
        l.flush();
        assertEquals(
                """
                        /tmp/manul/Test.mnl:2: ',' expected
                                var name: string
                                                ^
                        """,
                bout.toString().replace(System.lineSeparator(), "\n"));
    }

    private record MockSourceFile(String source) implements SourceFile {

        @Override
        public String getPath() {
            return "/tmp/manul/Test.mnl";
        }

        @Override
        public CharBuffer getContent() {
            return CharBuffer.wrap(source.toCharArray());
        }
    }

}