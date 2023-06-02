package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.PatternLab;

public class TransformPipelineTest extends TestCase {


    public void test() {
        var type = TranspileTestHelper.getType(PatternLab.class);
        new PatternTransformer().transform(type);
        System.out.println(type.toString());
    }

}