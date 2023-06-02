package tech.metavm.transpile2;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.spoon.FunctionalLab;

public class FunctionalWriterTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(FunctionalLab.class);
        var it = type.getMethods().iterator().next().getParameters().get(0).getType();

        Assert.assertTrue(TranspileUtil.isFunctionalType(it));

        new FunctionalWriter(
                new ConsoleTokenOutput(),
                new TranspileContext(type, type.getFactory())
        ).write(it);

    }

}