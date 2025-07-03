package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.diag.DummyLog;
import org.metavm.compiler.syntax.Call;
import org.metavm.util.TestUtils;

@Slf4j
public class LowerTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "kiwi/enum.kiwi");
        var file = CompilerTestUtils.parse(source);
        var proj = CompilerTestUtils.attr(file);
        file.accept(new Lower(proj, new DummyLog()));

        var classDecl = file.getClassDeclarations().getFirst();
        var impls = classDecl.getImplements();
        Assert.assertTrue(impls.nonEmpty());
        var ext = (Call) impls.head().getExpr();
        Assert.assertEquals(2, ext.getArguments().size());
    }

}
