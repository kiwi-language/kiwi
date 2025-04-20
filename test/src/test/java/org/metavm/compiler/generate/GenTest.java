package org.metavm.compiler.generate;

import junit.framework.TestCase;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.analyze.Lower;
import org.metavm.util.TestUtils;

public class GenTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath("kiwi/Shopping.kiwi");
        var file = CompilerTestUtils.parse(source);
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project));
        var gen = new Gen(project);
        file.accept(gen);
    }

}
