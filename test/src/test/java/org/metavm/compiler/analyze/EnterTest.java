package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.syntax.AstBuilder;
import org.metavm.compiler.util.List;
import org.metavm.util.TestUtils;


@Slf4j
public class EnterTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "kiwi/Shopping.kiwi");
        var file = AstBuilder.build(CompilerTestUtils.antlrParse(source));

        var project = new Project();
        var enter = new Enter(project);
        enter.enter(List.of(file));

        var pkg = project.getRootPackage();
        var classes = pkg.getClasses();
        Assert.assertEquals(5, classes.size());

        var productClass =  pkg.getClass(NameTable.instance.get("Product"));
        Assert.assertEquals("Product", productClass.getName().toString());
        Assert.assertEquals(2, productClass.getFields().size());
        Assert.assertEquals(5, productClass.getMethods().size());

        var couponStateClass = pkg.getClass(NameTable.instance.get("CouponState"));
        Assert.assertEquals("CouponState", couponStateClass.getName().toString());
        Assert.assertEquals(2, couponStateClass.getEnumConstants().size());
    }

}
