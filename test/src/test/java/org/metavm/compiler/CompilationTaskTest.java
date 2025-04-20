package org.metavm.compiler;

import junit.framework.TestCase;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

public class CompilationTaskTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "kiwi/Shopping.kiwi");
        var task = new CompilationTask(List.of(source), TestConstants.TARGET);
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        task.generate();
    }

}
