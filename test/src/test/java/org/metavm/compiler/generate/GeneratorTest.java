package org.metavm.compiler.generate;

import junit.framework.TestCase;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.analyze.Lower;

public class GeneratorTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        var file = CompilerTestUtils.parse(source);
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project));
        var gen = new Generator();
        file.accept(gen);
    }

}
