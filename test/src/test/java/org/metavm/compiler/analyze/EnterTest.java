package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.diag.DummyLog;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.syntax.Parser;
import org.metavm.compiler.util.List;
import org.metavm.util.TestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
public class EnterTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath( "kiwi/shopping.kiwi");
        var file = parse(source);

        var project = new Project();
        var enter = new Enter(project, new DummyLog());
        enter.enter(List.of(file));

        var pkg = project.getRootPackage();
        var classes = pkg.getClasses();
        Assert.assertEquals(5, classes.size());

        var productClass =  pkg.getClass(NameTable.instance.get("Product"));
        Assert.assertEquals("Product", productClass.getName().toString());
        Assert.assertEquals(3, productClass.getFields().size());
        Assert.assertEquals(2, productClass.getMethods().size());
    }


    private File parse(String path) {
        try {
            return new Parser(new DummyLog(), Files.readString(Path.of(path))).file();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
