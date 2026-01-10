package org.manul.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.manul.compiler.CompilerTestUtils;
import org.manul.compiler.diag.DummyLog;
import org.manul.compiler.syntax.Call;
import org.manul.util.TestUtils;

@Slf4j
public class LowerTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "manul/enum.mnl");
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
