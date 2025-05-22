package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.util.TestUtils;

@Slf4j
public class LowerTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "kiwi/enum.kiwi");
        var file = CompilerTestUtils.parse(source);
        var proj = CompilerTestUtils.attr(file);
        file.accept(new Lower(proj));

        var classDecl = file.getClassDeclarations().getFirst();
        var impls = classDecl.getImplements();
        Assert.assertTrue(impls.nonEmpty());
        var ext = impls.head();
        Assert.assertEquals(2, ext.getArgs().size());
    }

}
