package org.metavm.compiler.generate;

import junit.framework.TestCase;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.analyze.Lower;
import org.metavm.compiler.diag.DummyLog;
import org.metavm.util.TestUtils;

public class GenTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath("kiwi/Shopping.kiwi");
        var file = CompilerTestUtils.parse(source);
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project, new DummyLog()));
        var gen = new Gen(project, new DummyLog());
        file.accept(gen);
    }

}
