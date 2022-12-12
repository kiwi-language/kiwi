package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.meta.Field;
import tech.metavm.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.meta.TypeUtil.getArrayType;
import static tech.metavm.util.InstanceUtils.*;
import static tech.metavm.util.MockRegistry.*;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceTest.class);

    public static final String CONST_BAR_CODE = "001";
    public static final String CONST_FOO_NAME = "傻瓜一号";

    @Override
    protected void setUp() {
        ContextUtil.setContextInfo(TENANT_ID, -1L);
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testBar() {
        ClassInstance instance = getBarInstance();
        Assert.assertEquals(CONST_BAR_CODE, instance.getString(getField(Bar.class, "code")).getValue());
    }

    private ClassInstance getBarInstance() {
        Map<Field, Instance> barData = new HashMap<>();
        barData.put(getField(Bar.class, "code"), stringInstance(CONST_BAR_CODE));
        ClassInstance bar = new ClassInstance(barData, getClassType(Bar.class));
        bar.initId(2L);
        return bar;
    }

    private ClassInstance getFooInstance() {
        Map<Field, Instance> fooData = new HashMap<>();
        fooData.put(getField(Foo.class, "name"), stringInstance(CONST_FOO_NAME));
        fooData.put(getField(Foo.class, "bar"), getBarInstance());
        ClassInstance foo =  new ClassInstance(fooData, getClassType(Foo.class));
        foo.initId(1L);
        return foo;
    }

    public void testToPO() {
        Instance foo = getFooInstance();
        TestUtils.logJSON(LOGGER, foo.toPO(TENANT_ID));
    }

    public void testFoo() {
        ClassInstance foo = getFooInstance();
        ClassInstance bar = foo.getClassInstance(getField(Foo.class, "bar"));

        Assert.assertEquals(CONST_FOO_NAME, foo.getString(getField(Foo.class, "name")).getValue());
        Assert.assertNotNull(bar);
        Assert.assertEquals(CONST_BAR_CODE, bar.getString(getField(Bar.class, "code")).getValue());
    }

    public void testChildren() {
        Map<Field, Instance> data = Map.of(
                getField(Baz.class, "bars"),
                new ArrayInstance(
                        getArrayType(getClassType(Bar.class)),
                        List.of(getBarInstance()),
                        true
                )
        );
        ClassInstance baz = new ClassInstance(data, getClassType(Baz.class));
        ArrayInstance bars = baz.getInstanceArray(getField(Baz.class, "bars"));
        ClassInstance bar = (ClassInstance) bars.getInstance(0);
        Assert.assertEquals(1, bars.length());
        Assert.assertEquals(CONST_BAR_CODE, bar.getString(getField(Bar.class, "code")).getValue());
    }


}