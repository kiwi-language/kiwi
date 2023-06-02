package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.NullableVarLab;

public class NullableVariableTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(NullableVarLab.class);
        new NullableVariableTransformer().transform(type, new TranspileContext(type));
        System.out.println(type);
    }

}