package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.flow.MethodBuilder;
import org.metavm.util.Constants;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Set;

public class TypeParserImplTest extends TestCase {

    public void testFunctionType() {
        var type = TypeParser.parseType("()->any", id -> {
            throw new UnsupportedOperationException();
        });
        Assert.assertTrue(type instanceof FunctionType);
        var funcType = (FunctionType) type;
        Assert.assertTrue(funcType.getParameterTypes().isEmpty());
        Assert.assertTrue(funcType.getReturnType() instanceof AnyType);
    }

    public void testMethodRef() {
        var fooKlass = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        var testMethod = MethodBuilder.newBuilder(fooKlass, "test", "test").build();
        TestUtils.initEntityIds(fooKlass);
        var methodRef = TypeParser.parseMethodRef(
                String.format("%s.%s", fooKlass.getType().toExpression(), Constants.ID_PREFIX + testMethod.getStringId()),
                id -> {
                    if (fooKlass.idEquals(id))
                        return fooKlass;
                    else
                        throw new RuntimeException();
                }
        );
        Assert.assertSame(testMethod, methodRef.resolve());
    }

    public void testPrecedence() {
        var type = TypeParser.parseType("()->any|null", id -> {
            throw new UnsupportedOperationException();
        });
        Assert.assertEquals(new FunctionType(List.of(), new UnionType(Set.of(PrimitiveType.createNull(), new AnyType()))), type);
    }

}