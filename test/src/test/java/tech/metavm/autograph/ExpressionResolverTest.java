package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.mocks.TypeProviders;

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