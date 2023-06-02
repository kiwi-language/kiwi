package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.ReplacingLab;

public class MethodReplacingTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(ReplacingLab.class);
        new MethodReplacingTransformer().transform(type, new TranspileContext(type));
        new TypeReplacingTransformer().transform(type);
        System.out.println(type);
    }

}