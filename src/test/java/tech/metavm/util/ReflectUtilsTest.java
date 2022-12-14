package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Foo;
import tech.metavm.object.meta.ConstraintRT;

import java.lang.reflect.Type;
import java.util.List;

public class ReflectUtilsTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtilsTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testEraseType() {
        //noinspection rawtypes
        Type rawType = new TypeReference<Table<Table<ConstraintRT>>>() {}.getGenericType();
        Type type = new TypeReference<Table<Table<ConstraintRT<?>>>>() {}.getGenericType();
        Type erasedType = ReflectUtils.eraseType(type);
        Assert.assertEquals(rawType, erasedType);
    }

    public void testExtractReferences() {
        Foo foo = MockRegistry.getFoo();
        List<Reference> references = ReflectUtils.extractReferences(List.of(foo), t -> true);
        LOGGER.info(references.toString());
    }

}