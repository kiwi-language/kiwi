package org.metavm.autograph;

import junit.framework.TestCase;
import org.junit.Assert;

public class QnFactoryTest extends TestCase {

    public void test() {
        var psiFile = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.QnFoo");
        var klass = psiFile.getClasses()[0];
        var method = klass.getMethods()[0];
        var returnType = method.getReturnType();
        Assert.assertNotNull(returnType);
    }

}