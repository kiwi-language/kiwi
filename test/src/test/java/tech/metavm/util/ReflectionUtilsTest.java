package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.mocks.Foo;
import tech.metavm.object.type.Constraint;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectionUtilsTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtilsTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
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
        Foo foo = MockUtils.getFoo();
        List<Reference> references = EntityUtils.extractReferences(List.of(foo), t -> true);
        LOGGER.info(references.toString());
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