package org.metavm.compiler;

import junit.framework.TestCase;
import org.metavm.compiler.util.List;
import org.metavm.util.TestConstants;

public class CompilationTaskTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        var task = new CompilationTask(List.of(source), TestConstants.TARGET);
        task.parse();
        CompilerTestUtils.enterStandard(task.getProject());
        task.analyze();
        task.generate();
    }

}
