package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.SpreadLab;

public class InvocationArgumentSpreaderTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(SpreadLab.class);
        new InvocationArgumentSpreader().transform(type, new TranspileContext(type));
        System.out.println(type);
    }

}