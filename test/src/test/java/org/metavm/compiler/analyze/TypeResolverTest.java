package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.diag.DummyLog;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Field;
import org.metavm.compiler.syntax.FieldDecl;
import org.metavm.compiler.syntax.Import;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;
import org.metavm.util.TestUtils;


@Slf4j
public class TypeResolverTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath("kiwi/shopping.kiwi");
        var file = CompilerTestUtils.parse(source);

        var project = MockEnter.enter(List.of(file));
        MockEnter.enterStandard(project);

        ImportResolver.resolve(file, project, new DummyLog());
        for (Import imp : file.getImports()) {
            Assert.assertEquals(1, imp.getElements().size());
        }
        var typeResolver = new TypeResolver(project, new DummyLog());
        file.accept(typeResolver);

        var productClass = file.getClassDeclarations().getFirst().getElement();

        var nameField = productClass.getFieldByName("name");
        Assert.assertSame(Types.instance.getStringType(), nameField.getType());
    }

    public void testTypeVariable() {
        var source = TestUtils.getResourcePath("kiwi/box.kiwi");
        var file = CompilerTestUtils.parse(source);
        var project = MockEnter.enter(List.of(file));
        var typeResolver = new TypeResolver(project, new DummyLog());
        file.accept(typeResolver);
        var classDecl = file.getClassDeclarations().getFirst();
        var clazz = (Clazz) classDecl.getElement();
        var typeVar = clazz.getTypeParams().head();
        var fieldDecl = (FieldDecl) classDecl.getMembers().head();
        var field = (Field) fieldDecl.getElement();
        Assert.assertSame(typeVar, field.getType());
    }

}
