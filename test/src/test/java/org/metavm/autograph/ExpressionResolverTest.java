package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.object.type.ClassTypeBuilder;

public class ExpressionResolverTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testNewArrayList() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var barMethod = MethodBuilder.newBuilder(fooType, "bar", "bar")
                .build();
    }

}