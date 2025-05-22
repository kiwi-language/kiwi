package org.metavm.compiler.syntax;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;

import java.io.File;
import java.util.Objects;


@Slf4j
public class AstBuilderTest extends TestCase {

    public static final char[] buf = new char[1024 * 1024];

    public void test() {
        var source = TestUtils.getResourcePath("kiwi/Shopping.kiwi");
        var file = AstBuilder.build(CompilerTestUtils.antlrParse(source));
        var k = Utils.find(file.getClassDeclarations(), c -> c.tag() == ClassTag.ENUM);
        Assert.assertEquals(3, file.getImports().size());
        Assert.assertNotNull(k);
        Assert.assertEquals("CouponState", k.name().toString());
        Assert.assertEquals(2, k.enumConstants().size());
    }

    public void testDDL() {
        var source = TestUtils.getResourcePath("kiwi/ddl_before.kiwi");
        AstBuilder.build(CompilerTestUtils.antlrParse(source));
        var source1 = TestUtils.getResourcePath("kiwi/ddl_after.kiwi");
        var unit1 = AstBuilder.build(CompilerTestUtils.antlrParse(source1));
    }

    public void testAllFiles() {
        var dir = new File(TestUtils.getResourcePath("kiwi"));
        assert dir.isDirectory();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith(".kiwi")) {
                AstBuilder.build(CompilerTestUtils.antlrParse(file.getPath()));
            }
        }
    }

    public void testClassParams() {
        var source = TestUtils.getResourcePath("kiwi/class_params.kiwi");
        var file = AstBuilder.build(CompilerTestUtils.antlrParse(source));
        var klass = file.getClassDeclarations().head();
        Assert.assertEquals(1, klass.getParams().size());
    }

}