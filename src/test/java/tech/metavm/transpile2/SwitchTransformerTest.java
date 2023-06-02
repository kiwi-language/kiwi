package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.SwitchLab;

public class SwitchTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(SwitchLab.class);
        new SwitchTransformer().transform(type);
        System.out.println(type);
        System.out.println();
        new PatternTransformer().transform(type);
        System.out.println(type);
    }

}