package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.util.MockUtils;

import java.util.Set;

public class TypeUnionTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testIsAssignable() {
        var fooTypes = MockUtils.createFooTypes();
        var fooType = fooTypes.fooType();
        var barType = fooTypes.barType();
        var bazType = fooTypes.bazType();
        var livingBeingTypes = MockUtils.createLivingBeingTypes(false);
        var livingBeingType = livingBeingTypes.livingBeingType();
        var humanType = livingBeingTypes.humanType();

        UnionType unionType = new UnionType(null, Set.of(fooType, barType, livingBeingType));
        Assert.assertTrue(unionType.isAssignableFrom(fooType, null));
        Assert.assertTrue(unionType.isAssignableFrom(barType, null));
        Assert.assertFalse(unionType.isAssignableFrom(bazType, null));
        Assert.assertTrue(unionType.isAssignableFrom(new UnionType(null, Set.of(fooType, barType)), null));
        Assert.assertFalse(unionType.isAssignableFrom(new UnionType(null, Set.of(fooType, bazType)), null));
        Assert.assertTrue(unionType.isAssignableFrom(humanType, null));
    }

}