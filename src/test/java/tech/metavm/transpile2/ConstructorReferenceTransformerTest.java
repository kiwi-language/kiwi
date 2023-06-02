package tech.metavm.transpile2;

import junit.framework.TestCase;
import tech.metavm.spoon.ConstructorRefLab;

public class ConstructorReferenceTransformerTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getType(ConstructorRefLab.class);
        new ConstructorReferenceTransformer().transform(type);
        System.out.println(type);
    }

}