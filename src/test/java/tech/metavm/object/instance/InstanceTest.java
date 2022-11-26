package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.meta.Field;
import tech.metavm.util.*;
import tech.metavm.util.UTTypes.ArrayTypes;
import tech.metavm.util.UTTypes.Fields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceTest extends TestCase {

    public static final String CONST_BAR_CODE = "001";
    public static final String CONST_FOO_NAME = "傻瓜一号";

    public void testBar() {
        Instance instance = getBarInstance();
        Assert.assertEquals(CONST_BAR_CODE, instance.getString(Fields.BAR_CODE));
    }

    private Instance getBarInstance() {
        Map<Field, Object> barData = new HashMap<>();
        barData.put(Fields.BAR_CODE, CONST_BAR_CODE);
        return new Instance(barData, UTTypes.BAR, Bar.class);
    }

    public void testFoo() {
        Map<Field, Object> fooData = new HashMap<>();
        fooData.put(Fields.FOO_NAME, CONST_FOO_NAME);
        fooData.put(Fields.FOO_BAR, getBarInstance());
        Instance foo = new Instance(fooData, UTTypes.FOO, Foo.class);
        IInstance bar = foo.getInstance(Fields.FOO_BAR);

        Assert.assertEquals(CONST_FOO_NAME, foo.getString(Fields.FOO_NAME));
        Assert.assertNotNull(bar);
        Assert.assertEquals(CONST_BAR_CODE, bar.getString(Fields.BAR_CODE));
    }

    public void testChildren() {
        Map<Field, Object> data = Map.of(
                Fields.BAZ_BARS,
                new InstanceArray(
                        ArrayTypes.BAR_ARRAY,
                        Table.class,
                        List.of(getBarInstance()),
                        true
                )
        );
        Instance baz = new Instance(data, UTTypes.BAZ, Baz.class);
        InstanceArray bars = baz.getInstanceArray(Fields.BAZ_BARS);
        Assert.assertEquals(1, bars.length());
        Assert.assertEquals(CONST_BAR_CODE, bars.get(0).getString(Fields.BAR_CODE));
    }


}