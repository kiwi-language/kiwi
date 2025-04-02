package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.element.ClassTag;
import org.metavm.compiler.syntax.ClassDecl;

@Slf4j
public class TransformerTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        var file = CompilerTestUtils.parse(source);
        var proj = CompilerTestUtils.attr(file);
        var transformer = new Lower(proj);
        file.accept(transformer);
        var classDecl = file.getClassDeclarations().find(
                decl -> decl instanceof ClassDecl c && c.name().toString().equals("CouponState")
        );
        Assert.assertNotNull(classDecl);
        var clazz = classDecl.getElement();
        Assert.assertEquals(ClassTag.ENUM, clazz.getClassTag());
        log.debug("{}", classDecl.getText());
    }

}
