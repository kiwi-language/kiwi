package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.util.MockUtils;

import java.util.Set;

public class TypeUnionTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testIsAssignable() {
        var fooTypes = MockUtils.createFooTypes();
        var fooType = fooTypes.fooType().getType();
        var barType = fooTypes.barType().getType();
        var bazType = fooTypes.bazType().getType();
        var livingBeingTypes = MockUtils.createLivingBeingTypes(false);
        var livingBeingType = livingBeingTypes.livingBeingType().getType();
        var humanType = livingBeingTypes.humanType().getType();

        UnionType unionType = new UnionType(Set.of(fooType, barType, livingBeingType));
        Assert.assertTrue(unionType.isAssignableFrom(fooType));
        Assert.assertTrue(unionType.isAssignableFrom(barType));
        Assert.assertFalse(unionType.isAssignableFrom(bazType));
        Assert.assertTrue(unionType.isAssignableFrom(new UnionType(Set.of(fooType, barType))));
        Assert.assertFalse(unionType.isAssignableFrom(new UnionType(Set.of(fooType, bazType))));
        Assert.assertTrue(unionType.isAssignableFrom(humanType));
    }

}