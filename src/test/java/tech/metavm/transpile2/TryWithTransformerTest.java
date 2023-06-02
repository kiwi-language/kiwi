package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.TryWithLab;

public class TryWithTransformerTest extends TestCase {

    public void test() {
        var klass = TranspileTestHelper.getCtClass(TryWithLab.class);
        new TryWithTransformer().transform(klass, new TranspileContext(klass));
        System.out.println(klass);
    }

}