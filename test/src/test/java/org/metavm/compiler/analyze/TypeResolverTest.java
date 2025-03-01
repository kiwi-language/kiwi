package org.metavm.compiler.analyze;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Field;
import org.metavm.compiler.syntax.FieldDecl;
import org.metavm.compiler.syntax.Import;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.util.List;


@Slf4j
public class TypeResolverTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        var file = CompilerTestUtils.parse(source);

        var project = CompilerTestUtils.enter(List.of(file));
        CompilerTestUtils.enterStandard(project);

        ImportResolver.resolve(file, project);
        for (Import imp : file.getImports()) {
            Assert.assertEquals(1, imp.getElements().size());
        }
        var typeResolver = new TypeResolver();
        file.accept(typeResolver);

        var productClass = file.getClassDeclarations().getFirst().getElement();
        var skuClass = project.classForName("SKU");

        var nameField = productClass.getFieldByName("name");
        Assert.assertSame(PrimitiveType.STRING, nameField.getType());

        var getSkuListMethod = productClass.getMethodsByName("getSkuList").next();
        var listClazz = project.classForName("java.util.List");
        Assert.assertSame(
                listClazz.getType(null, List.of(skuClass.getType())),
                getSkuListMethod.getReturnType()
        );
    }

    public void testTypeVariable() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/box.kiwi";
        var file = CompilerTestUtils.parse(source);
        CompilerTestUtils.enter(List.of(file));
        var typeResolver = new TypeResolver();
        file.accept(typeResolver);
        var classDecl = file.getClassDeclarations().getFirst();
        var clazz = (Clazz) classDecl.getElement();
        var typeVar = clazz.getTypeParameters().head();
        var fieldDecl = (FieldDecl) classDecl.getMembers().getFirst();
        var field = (Field) fieldDecl.getElement();
        Assert.assertSame(typeVar, field.getType());
    }

}
