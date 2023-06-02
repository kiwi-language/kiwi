package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.ListSizeLab;

public class ListSizeReplacingTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getCtClass(ListSizeLab.class);
        new ListSizeReplacingTransformer().transform(type, new TranspileContext(type));
        System.out.println(type);
    }

}