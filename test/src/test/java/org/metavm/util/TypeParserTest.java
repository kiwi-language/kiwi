package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.mocks.Foo;
import org.metavm.object.type.Constraint;
import org.metavm.object.type.Klass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TypeParserTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(TypeParser.class);

    public void testClass() {
        Type parsedClass = TypeParser.parse(Foo.class.getName());
        Assert.assertEquals(Foo.class, parsedClass);
    }

    public void testInnerClass() {
        ParameterizedType parameterizedType = new ParameterizedTypeImpl(null,
                Table.class, new Type[] {InnerClass.class});
        Type parsedType = TypeParser.parse(parameterizedType.getTypeName());
        Assert.assertEquals(parsedType, parsedType);
    }

    public void test_multiple_type_arguments() {
        ParameterizedType parameterizedType = new ParameterizedTypeImpl(null,
                Map.class, new Type[] {String.class, InnerClass.class});
        Type parsedType = TypeParser.parse(parameterizedType.getTypeName());
        Assert.assertEquals(parsedType, parsedType);
    }

    public void test_nested_parameterized_type() {
        ParameterizedType parameterizedType = ParameterizedTypeImpl.create(
                Map.class,
                ParameterizedTypeImpl.create(ParameterizedKey.class, String.class),
                ParameterizedTypeImpl.create(Table.class, InnerClass.class)
        );
        Type parsedType = TypeParser.parse(parameterizedType.getTypeName());
        Assert.assertEquals(parsedType, parsedType);
    }

    public void testAsteriskWildcard() {
        ParameterizedType parameterizedType = ParameterizedTypeImpl.create(
                Table.class,
                ParameterizedTypeImpl.create(Constraint.class, WildcardTypeImpl.createAsterisk())
        );
        Type parsedType = TypeParser.parse(parameterizedType.getTypeName());
        Assert.assertEquals(parsedType, parsedType);
    }

    public void testExtendsWildcard() {
        ParameterizedType parameterizedType = ParameterizedTypeImpl.create(
                Table.class,
                ParameterizedTypeImpl.create(
                        List.class,
                        WildcardTypeImpl.createExtends(org.metavm.object.type.Type.class)
                )
        );
        Type parsedType = TypeParser.parse(parameterizedType.getTypeName());
        Assert.assertEquals(parsedType, parsedType);
    }

    public void testSuperWildcard() {
        ParameterizedType parameterizedType = ParameterizedTypeImpl.create(
                Table.class,
                ParameterizedTypeImpl.create(
                        List.class,
                        WildcardTypeImpl.createSuper(Klass.class)
                )
        );
        Type parsedType = TypeParser.parse(parameterizedType.getTypeName());
        Assert.assertEquals(parsedType, parsedType);
    }

    private static class ParameterizedKey<T> {
        T key;
    }

    private static class InnerClass {}

}