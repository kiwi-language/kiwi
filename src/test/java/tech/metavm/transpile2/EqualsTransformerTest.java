package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.EqualsLab;

public class EqualsTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(EqualsLab.class);
        new EqualsTransformer().transform(type);
        System.out.println(type);
    }

}