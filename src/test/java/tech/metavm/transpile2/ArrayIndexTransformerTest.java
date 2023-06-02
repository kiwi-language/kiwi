package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.ReplacingLab;

public class ArrayIndexTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(ReplacingLab.class);
        new ArrayIndexTransformer().transform(type);
        System.out.println(type);
    }

}