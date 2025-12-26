package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.util.ContextUtil;
import org.metavm.util.FooTypes;
import org.metavm.util.MockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metavm.util.Instances.stringInstance;
import static org.metavm.util.TestConstants.APP_ID;

public class InstanceTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(InstanceTest.class);

    public static final String CONST_BAR_CODE = "001";
    public static final String CONST_FOO_NAME = "foo1";

    @Override
    protected void setUp() {
        ContextUtil.setAppId(APP_ID);
    }

    public void testBar() {
        var fooTypes = MockUtils.createFooTypes(false);
        ClassInstance instance = getBarInstance(fooTypes);
        Assert.assertEquals(CONST_BAR_CODE, instance.getStringField(fooTypes.barCodeField()));
    }

    private ClassInstance getBarInstance(FooTypes fooTypes) {
        Map<Field, Value> barData = new HashMap<>();
        barData.put(fooTypes.barCodeField(), stringInstance(CONST_BAR_CODE));
        return ClassInstance.create(TmpId.random(), barData, fooTypes.barType().getType());
    }

    private ClassInstance getFooInstance(FooTypes fooTypes) {
        Map<Field, Value> fooData = new HashMap<>();
        fooData.put(fooTypes.fooNameField(), stringInstance(CONST_FOO_NAME));
        fooData.put(fooTypes.fooBarsField(),
                new ArrayInstance(
                        fooTypes.barChildArrayType(),
                        List.of(getBarInstance(fooTypes).getReference())
                ).getReference()
        );
        fooData.put(fooTypes.fooBazListField(), new ArrayInstance(fooTypes.bazArrayType()).getReference());
        return ClassInstance.create(TmpId.random(), fooData, fooTypes.fooType().getType());
    }

    public void testFoo() {
        var fooTypes = MockUtils.createFooTypes(false);
        var foo = getFooInstance(fooTypes);
        var bar = foo.getInstanceArray(fooTypes.fooBarsField()).getFirst().resolveObject();
        Assert.assertEquals(CONST_FOO_NAME, foo.getStringField(fooTypes.fooNameField()));
        Assert.assertNotNull(bar);
        Assert.assertEquals(CONST_BAR_CODE, bar.getStringField(fooTypes.barCodeField()));
    }

    public void testChildren() {
        var fooTypes = MockUtils.createFooTypes(false);
        Map<Field, Value> data = Map.of(
                fooTypes.fooNameField(), stringInstance(CONST_FOO_NAME),
                fooTypes.fooBarsField(),
                new ArrayInstance(
                        fooTypes.barChildArrayType(),
                        List.of(getBarInstance(fooTypes).getReference())
                ).getReference(),
                fooTypes.fooBazListField(),
                new ArrayInstance(fooTypes.bazArrayType()).getReference()
        );
        var foo = ClassInstance.create(TmpId.random(), data, fooTypes.fooType().getType());
        var bars = foo.getInstanceArray(fooTypes.fooBarsField());
        var bar = bars.getInstance(0).resolveObject();
        Assert.assertEquals(1, bars.length());
        Assert.assertEquals(CONST_BAR_CODE, bar.getStringField(fooTypes.barCodeField()));
    }


}