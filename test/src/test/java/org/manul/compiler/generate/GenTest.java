package org.manul.compiler.generate;

import junit.framework.TestCase;
import org.manul.compiler.CompilerTestUtils;
import org.manul.compiler.analyze.Lower;
import org.manul.compiler.diag.DummyLog;
import org.manul.util.TestUtils;

public class GenTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath("manul/shopping.mnl");
        var file = CompilerTestUtils.parse(source);
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project, new DummyLog()));
        var gen = new Gen(project, new DummyLog());
        file.accept(gen);
    }

}
