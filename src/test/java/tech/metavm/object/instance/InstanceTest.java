package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.meta.Field;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.TestUtils;
import tech.metavm.util.UTTypes;
import tech.metavm.util.UTTypes.ArrayTypes;
import tech.metavm.util.UTTypes.Fields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceTest.class);

    public static final String CONST_BAR_CODE = "001";
    public static final String CONST_FOO_NAME = "傻瓜一号";

    @Override
    protected void setUp() {
        ContextUtil.setContextInfo(-1L, -1L);
    }

    public void testBar() {
        Instance instance = getBarInstance();
        Assert.assertEquals(CONST_BAR_CODE, instance.getString(Fields.BAR_CODE));
    }

    private Instance getBarInstance() {
        Map<Field, Object> barData = new HashMap<>();
        barData.put(Fields.BAR_CODE, CONST_BAR_CODE);
        Instance bar = new Instance(barData, UTTypes.BAR);
        bar.initId(2L);
        return bar;
    }

    private Instance getFooInstance() {
        Map<Field, Object> fooData = new HashMap<>();
        fooData.put(Fields.FOO_NAME, CONST_FOO_NAME);
        fooData.put(Fields.FOO_BAR, getBarInstance());
        Instance foo =  new Instance(fooData, UTTypes.FOO);
        foo.initId(1L);
        return foo;
    }

    public void testToPO() {
        Instance foo = getFooInstance();
        TestUtils.logJSON(LOGGER, foo.toPO(TENANT_ID));
    }

    public void testFoo() {
        Instance foo = getFooInstance();
        Instance bar = foo.getInstance(Fields.FOO_BAR);

        Assert.assertEquals(CONST_FOO_NAME, foo.getString(Fields.FOO_NAME));
        Assert.assertNotNull(bar);
        Assert.assertEquals(CONST_BAR_CODE, bar.getString(Fields.BAR_CODE));
    }

    public void testChildren() {
        Map<Field, Object> data = Map.of(
                Fields.BAZ_BARS,
                new InstanceArray(
                        ArrayTypes.BAR_ARRAY,
                        List.of(getBarInstance()),
                        true
                )
        );
        Instance baz = new Instance(data, UTTypes.BAZ);
        InstanceArray bars = baz.getInstanceArray(Fields.BAZ_BARS);
        Assert.assertEquals(1, bars.length());
        Assert.assertEquals(CONST_BAR_CODE, bars.getInstance(0).getString(Fields.BAR_CODE));
    }


}