package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.PersistenceUtils;
import tech.metavm.object.type.Field;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.FooTypes;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.Instances.stringInstance;
import static tech.metavm.util.TestConstants.APP_ID;

public class InstanceTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceTest.class);

    public static final String CONST_BAR_CODE = "001";
    public static final String CONST_FOO_NAME = "傻瓜一号";

    @Override
    protected void setUp() {
        ContextUtil.setAppId(APP_ID);
        MockStandardTypesInitializer.init();
    }

    public void testBar() {
        var fooTypes = MockUtils.createFooTypes(false);
        ClassInstance instance = getBarInstance(fooTypes);
        Assert.assertEquals(CONST_BAR_CODE, instance.getStringField(fooTypes.barCodeField()).getValue());
    }

    private ClassInstance getBarInstance(FooTypes fooTypes) {
        Map<Field, Instance> barData = new HashMap<>();
        barData.put(fooTypes.barCodeField(), stringInstance(CONST_BAR_CODE));
        ClassInstance bar = ClassInstance.create(barData, fooTypes.barType());
        bar.initId(DefaultPhysicalId.ofObject(2L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        return bar;
    }

    private ClassInstance getFooInstance(FooTypes fooTypes) {
        Map<Field, Instance> fooData = new HashMap<>();
        fooData.put(fooTypes.fooNameField(), stringInstance(CONST_FOO_NAME));
        fooData.put(fooTypes.fooBarsField(),
                new ArrayInstance(
                        fooTypes.barChildArrayType(),
                        List.of(getBarInstance(fooTypes))
                )
        );
        fooData.put(fooTypes.fooBazListField(), new ArrayInstance(fooTypes.bazArrayType()));
        ClassInstance foo = ClassInstance.create(fooData, fooTypes.fooType());
        foo.initId(DefaultPhysicalId.ofObject(1L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        return foo;
    }

    public void testToPO() {
        var fooTypes = MockUtils.createFooTypes(true);
        var foo = getFooInstance(fooTypes);
        TestUtils.initInstanceIds(foo);
        TestUtils.logJSON(LOGGER, PersistenceUtils.toInstancePO(foo, APP_ID));
    }

    public void testFoo() {
        var fooTypes = MockUtils.createFooTypes(false);
        var foo = getFooInstance(fooTypes);
        var bar = (ClassInstance) foo.getInstanceArray(fooTypes.fooBarsField()).get(0);
        Assert.assertEquals(CONST_FOO_NAME, foo.getStringField(fooTypes.fooNameField()).getValue());
        Assert.assertNotNull(bar);
        Assert.assertEquals(CONST_BAR_CODE, bar.getStringField(fooTypes.barCodeField()).getValue());
    }

    public void testChildren() {
        var fooTypes = MockUtils.createFooTypes(false);
        Map<Field, Instance> data = Map.of(
                fooTypes.fooNameField(), stringInstance(CONST_FOO_NAME),
                fooTypes.fooBarsField(),
                new ArrayInstance(
                        fooTypes.barChildArrayType(),
                        List.of(getBarInstance(fooTypes))
                ),
                fooTypes.fooBazListField(),
                new ArrayInstance(fooTypes.bazArrayType())
        );
        var foo = ClassInstance.create(data, fooTypes.fooType());
        var bars = foo.getInstanceArray(fooTypes.fooBarsField());
        var bar = (ClassInstance) bars.getInstance(0);
        Assert.assertEquals(1, bars.length());
        Assert.assertEquals(CONST_BAR_CODE, bar.getStringField(fooTypes.barCodeField()).getValue());
    }


}