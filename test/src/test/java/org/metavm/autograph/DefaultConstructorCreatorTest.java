package org.metavm.autograph;

import com.intellij.psi.PsiModifier;
import junit.framework.TestCase;
import org.junit.Assert;

public class DefaultConstructorCreatorTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.DefaultConstructorFoo");
        TranspileTestTools.executeCommand((() -> file.accept(new DefaultConstructorCreator())));
        System.out.println(file.getText());
        var klass = file.getClasses()[0];
        var constructor = klass.getMethods()[0];
        Assert.assertTrue(constructor.isConstructor());
        Assert.assertTrue(constructor.hasModifierProperty(PsiModifier.PUBLIC));

        var enumClass = file.getClasses()[1];
        var enumConstructor = enumClass.getMethods()[0];
        Assert.assertTrue(enumConstructor.isConstructor());
        Assert.assertFalse(enumConstructor.hasModifierProperty(PsiModifier.PUBLIC));

        var withConstructorClass = file.getClasses()[2];
        Assert.assertEquals(1, withConstructorClass.getMethods().length);
    }

}