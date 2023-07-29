package tech.metavm.autograph;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.autograph.mocks.QnFoo;

public class QnFactoryTest extends TestCase {

    public void test() {
        var psiFile = TranspileTestTools.getPsiJavaFile(QnFoo.class);
        var klass = psiFile.getClasses()[0];
        var method = klass.getMethods()[0];
        var returnType = method.getReturnType();
        Assert.assertNotNull(returnType);
        System.out.println(QnFactory.getTypeQn(returnType));
        System.out.println(QnFactory.getMethodQn(method));
    }

}