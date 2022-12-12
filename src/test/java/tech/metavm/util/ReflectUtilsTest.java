package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.meta.ConstraintRT;

import java.lang.reflect.Type;

public class ReflectUtilsTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtilsTest.class);

    public void testEraseType() {
        //noinspection rawtypes
        Type rawType = new TypeReference<Table<Table<ConstraintRT>>>() {}.getGenericType();
        Type type = new TypeReference<Table<Table<ConstraintRT<?>>>>() {}.getGenericType();
        Type erasedType = ReflectUtils.eraseType(type);
        Assert.assertEquals(rawType, erasedType);
    }

}