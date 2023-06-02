package tech.metavm.transpile;

import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStreams;

import java.io.IOException;

public class IRBuilderTest extends TestCase {

    public static final String fileName = "/Users/leen/workspace/object/src/test/java/tech/metavm/mocks/Foo.java";

    public void test() throws IOException {
        IRBuilder builder = new IRBuilder(CharStreams.fromFileName(fileName));
        var sourceUnit = builder.parse();
        System.out.println(sourceUnit);
    }

}
