package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.util.Constants;
import tech.metavm.util.TestUtils;

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
                String.format("%s.%s", fooKlass.getType().toExpression(), Constants.CONSTANT_ID_PREFIX + testMethod.getStringId()),
                id -> {
                    if(fooKlass.idEquals(id))
                        return fooKlass;
                    else
                        throw new RuntimeException();
                }
        );
        Assert.assertSame(testMethod, methodRef.resolve());
    }

}