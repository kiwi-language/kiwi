package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.mocks.Foo;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectionUtilsTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ReflectionUtilsTest.class);

    public boolean value;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        value = false;
    }

    public void testEraseType() {
        Type rawType = new TypeReference<Table<Table<Constraint>>>() {
        }.getGenericType();
        Type type = new TypeReference<Table<Table<Constraint>>>() {
        }.getGenericType();
        Type erasedType = ReflectionUtils.eraseType(type);
        Assert.assertEquals(rawType, erasedType);
    }

    public void testExtractReferences() {
        Foo foo = MockUtils.getFooCreator().apply(TmpId::random);
        List<Reference> references = EntityUtils.extractReferences(List.of(foo), t -> true);
        logger.info(references.toString());
    }

    public void testResolveGenerics() {
        var substitutor = ReflectionUtils.resolveGenerics(
                new TypeReference<GenericsFoo<Map<String, Object>>>() {
                }.getGenericType()
        );

        Field field = ReflectionUtils.getField(GenericsBase.class, "value");
        var resolvedFieldValue = substitutor.substitute(field.getGenericType());
        var listStrType = new TypeReference<Set<Map<String, Object>>>() {
        }.getGenericType();
        assertEquals(listStrType, resolvedFieldValue);

        var barMethod = ReflectionUtils.getMethod(GenericInterface.class, "bar");
        var expectedBarType = new TypeReference<List<Collection<Set<Map<String, Object>>>>>() {
        }.getGenericType();
        var actualBarType = substitutor.substitute(barMethod.getGenericReturnType());
        assertEquals(expectedBarType, actualBarType);
    }

    public void testUnreflect() throws Throwable {
        var method = ReflectionUtils.getDeclaredMethod(ReflectionUtilsTest.class, "setValue", List.of());
        var mh = ReflectionUtils.unreflect(MethodHandles.lookup(), method);
        Assert.assertNull(mh.invokeExact(new Object[] {this}));
        Assert.assertTrue(value);
    }

    public void testGetMethodHandleWithSpread() throws Throwable {
        var mh = ReflectionUtils.getMethodHandleWithSpread(MethodHandles.lookup(), ReflectionUtilsTest.class, "setValue",
                void.class, List.of(), false);
        mh.invokeExact(new Object[] {this});
        Assert.assertTrue(value);
    }

    private void setValue() {
        value = true;
    }

    interface GenericInterface<V> {

        List<V> bar();

    }

    static abstract class GenericsBase<E> implements GenericInterface<Collection<E>>{

        @SuppressWarnings("unused")
        E value;

    }


    static class GenericsFoo<T> extends GenericsBase<Set<T>>  {

        @Override
        public List<Collection<Set<T>>> bar() {
            return null;
        }
    }

}