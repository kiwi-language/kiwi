package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.util.TestUtils;

public class ExpressionResolverTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testNewArrayList() {
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo").build();
        var barMethod = MethodBuilder.newBuilder(fooType, "bar")
                .build();
    }

}