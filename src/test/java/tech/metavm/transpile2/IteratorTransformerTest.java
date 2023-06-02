package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.IteratorLab;

public class IteratorTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(IteratorLab.class);
        new IteratorTransformer().transform(type);
        System.out.println(type);
    }

}