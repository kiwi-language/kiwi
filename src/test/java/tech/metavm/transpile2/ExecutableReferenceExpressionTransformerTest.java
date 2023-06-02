package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.ExeRefExprLab;

public class ExecutableReferenceExpressionTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getCtClass(ExeRefExprLab.class);
        new ExecutableReferenceExpressionTransformer().transform(type, new TranspileContext(type));
        System.out.println(type);
    }

}