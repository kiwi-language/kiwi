package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.ExceptionLab;
import tech.metavm.spoon.InvocationLab;
import tech.metavm.spoon.SetLab;
import tech.metavm.spoon.StreamLab;

public class InvocationTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getCtClass(InvocationLab.class);
        new InvocationTransformer().transform(type, new TranspileContext(type));
        System.out.println(type);
    }

    public void testStream() {
        var klass = TranspileTestHelper.getCtClass(StreamLab.class);
        new InvocationTransformer().transform(klass, new TranspileContext(klass));
        System.out.println(klass);
    }

    public void testSetAddAll() {
        var klass = TranspileTestHelper.getCtClass(SetLab.class);
        new InvocationTransformer().transform(klass, new TranspileContext(klass));
        System.out.println(klass);
    }

    public void testExceptionCause() {
        var klass = TranspileTestHelper.getCtClass(ExceptionLab.class);
        new InvocationTransformer().transform(klass, new TranspileContext(klass));
        System.out.println(klass);
    }

}