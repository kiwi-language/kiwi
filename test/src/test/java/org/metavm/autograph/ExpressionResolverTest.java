package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.object.type.KlassBuilder;

public class ExpressionResolverTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testNewArrayList() {
        var fooType = KlassBuilder.newBuilder("Foo", "Foo").build();
        var barMethod = MethodBuilder.newBuilder(fooType, "bar", "bar")
                .build();
    }

}