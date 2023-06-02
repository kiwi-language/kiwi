package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.*;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.Set;

public class TypeUnionTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testIsAssignable() {
        Type fooType = MockRegistry.getType(Foo.class);
        Type barType = MockRegistry.getType(Bar.class);
        Type bazType = MockRegistry.getType(Baz.class);
        Type livingBeingType = MockRegistry.getType(LivingBeing.class);
        Type humanType = MockRegistry.getType(Human.class);

        UnionType unionType = new UnionType(Set.of(fooType, barType, livingBeingType));
        Assert.assertTrue(unionType.isAssignableFrom(fooType));
        Assert.assertTrue(unionType.isAssignableFrom(barType));
        Assert.assertFalse(unionType.isAssignableFrom(bazType));
        Assert.assertTrue(unionType.isAssignableFrom(new UnionType(Set.of(fooType, barType))));
        Assert.assertFalse(unionType.isAssignableFrom(new UnionType(Set.of(fooType, bazType))));
        Assert.assertTrue(unionType.isAssignableFrom(humanType));
    }

}