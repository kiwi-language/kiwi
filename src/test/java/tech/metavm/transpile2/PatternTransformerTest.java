package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.PatternLab;

public class PatternTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(PatternLab.class);
        System.out.println(type);
        System.out.println();
        new PatternTransformer().transform(type);
        System.out.println(type);
    }

}