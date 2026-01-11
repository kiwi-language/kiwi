package org.manul.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.manul.compiler.CompilerTestUtils;
import org.manul.compiler.diag.DummyLog;
import org.manul.compiler.element.ClassTag;
import org.manul.compiler.syntax.ClassDecl;
import org.manul.util.TestUtils;

@Slf4j
public class TransformerTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath("manul/shopping.mnl");
        var file = CompilerTestUtils.parse(source);
        var proj = CompilerTestUtils.attr(file);
        var transformer = new Lower(proj, new DummyLog());
        file.accept(transformer);
        var classDecl = file.getClassDeclarations().find(
                decl -> decl instanceof ClassDecl c && c.name().toString().equals("OrderStatus")
        );
        Assert.assertNotNull(classDecl);
        var clazz = classDecl.getElement();
        Assert.assertEquals(ClassTag.ENUM, clazz.getClassTag());
    }

}
