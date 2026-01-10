package org.manul.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.manul.compiler.CompilerTestUtils;
import org.manul.compiler.diag.DummyLog;
import org.manul.compiler.element.Clazz;
import org.manul.compiler.element.Field;
import org.manul.compiler.syntax.FieldDecl;
import org.manul.compiler.syntax.Import;
import org.manul.compiler.type.Types;
import org.manul.compiler.util.List;
import org.manul.compiler.util.MockEnter;
import org.manul.util.TestUtils;


@Slf4j
public class TypeResolverTest extends TestCase {

    public void test() {
        var source = TestUtils.getResourcePath("manul/shopping.mnl");
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
        var source = TestUtils.getResourcePath("manul/box.mnl");
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
