package org.metavm.object.type;

import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.flow.MethodBuilder;
import org.metavm.util.Constants;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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
        var fooKlass = TestUtils.newKlassBuilder("Foo", "Foo").build();
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
        Assert.assertEquals(new FunctionType(List.of(), new UnionType(Set.of(PrimitiveType.nullType, AnyType.instance))), type);
    }

    public void testParseFunction() {
        var functionSig = "T requireNonNull2<T>(T|null value, java.util.function.Supplier<string> messageSupplier)";
        var supplierKlass = TestUtils.newKlassBuilder(Supplier.class)
                .typeParameters(new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE))
                .build();
        var func = new TypeParserImpl((String name) -> {
            if(name.equals(supplierKlass.getCode()))
                return supplierKlass;
            else
                throw new NullPointerException("No such class: " + name);
        }).parseFunction(functionSig);
        Assert.assertEquals("requireNonNull2", func.getName());
        Assert.assertEquals("requireNonNull2", func.getCode());
        Assert.assertEquals(1, func.getTypeParameters().size());
        var variableType = func.getTypeParameters().get(0).getType();
        Assert.assertEquals(variableType, func.getReturnType());
        Assert.assertEquals(2, func.getParameters().size());
        Assert.assertEquals("value", func.getParameter(0).getName());
        Assert.assertEquals("value", func.getParameter(0).getCode());
        Assert.assertEquals(new UnionType(Set.of(variableType, Types.getNullType())), func.getParameter(0).getType());
        Assert.assertEquals("messageSupplier", func.getParameter(1).getName());
        Assert.assertEquals("messageSupplier", func.getParameter(1).getCode());
        Assert.assertEquals(supplierKlass.getParameterized(List.of(Types.getStringType())).getType(), func.getParameter(1).getType());
    }


    public void testQualifiedClass() {
        var classCode = "capturedtypes.CtLab";
        var klass = TestUtils.newKlassBuilder("CtLab", classCode).build();
        ParserTypeDefProvider typeDefProvider = name -> {
            if(name.equals(classCode))
                return klass;
            else
                return null;
        };
        var input = CharStreams.fromString(classCode);
//        var parser = new org.metavm.object.type.antlr.TypeParser(new CommonTokenStream(new TypeLexer(input)));
//        var parser = new AssemblyParser(new CommonTokenStream(new AssemblyLexer(input)));
//        parser.setErrorHandler(new BailErrorStrategy());
        var parser = new TypeParserImpl(typeDefProvider);
        var type = parser.parseType(classCode);
        Assert.assertEquals(klass.getType(), type);
    }

}