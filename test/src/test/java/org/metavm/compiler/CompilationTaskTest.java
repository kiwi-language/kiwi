package org.metavm.compiler;

import junit.framework.TestCase;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.nio.file.Path;

public class CompilationTaskTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "kiwi/shopping.kiwi");
        var task = CompilationTaskBuilder.newBuilder(List.of(Path.of(source)), Path.of(TestConstants.TARGET)).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0)
            task.generate();
    }

    public void testAnalysisError() {
        var source = TestUtils.getResourcePath( "kiwi/error/resolve.kiwi");
        var task = CompilationTaskBuilder.newBuilder(List.of(Path.of(source)), Path.of(TestConstants.TARGET)).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        assertEquals(1, task.getErrorCount());
    }

}
