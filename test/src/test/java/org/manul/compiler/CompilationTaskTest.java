package org.manul.compiler;

import junit.framework.TestCase;
import org.manul.compiler.util.List;
import org.manul.compiler.util.MockEnter;
import org.manul.util.TestConstants;
import org.manul.util.TestUtils;

import java.nio.file.Path;

public class CompilationTaskTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "manul/shopping.mnl");
        var task = CompilationTaskBuilder.newBuilder(List.of(Path.of(source)), Path.of(TestConstants.TARGET)).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0)
            task.generate();
    }

    public void testAnalysisError() {
        var source = TestUtils.getResourcePath( "manul/error/resolve.mnl");
        var task = CompilationTaskBuilder.newBuilder(List.of(Path.of(source)), Path.of(TestConstants.TARGET)).build();
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        assertEquals(1, task.getErrorCount());
    }

}
