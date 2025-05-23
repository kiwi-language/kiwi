package org.metavm.object.type;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.Entity;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FunctionBuilder;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.MethodBuilder;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.util.Constants;
import org.metavm.util.TestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TypeParserImplTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
    }

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
        var testMethod = MethodBuilder.newBuilder(fooKlass, "test").build();
        var methodRef = TypeParser.parseMethodRef(
                String.format("%s::%s", fooKlass.getType().toExpression(), Constants.ID_PREFIX + testMethod.getStringId()),
                id -> {
                    if (fooKlass.idEquals(id))
                        return fooKlass;
                    else if(testMethod.idEquals(id))
                        return testMethod;
                    else
                        throw new RuntimeException();
                }
        );
        Assert.assertSame(testMethod, methodRef.getRawFlow());
    }

    public void testPrecedence() {
        var type = TypeParser.parseType("()->any|null", id -> {
            throw new UnsupportedOperationException();
        });
        Assert.assertEquals(new FunctionType(List.of(), new UnionType(Set.of(NullType.instance, AnyType.instance))), type);
    }

    public void testParseFunction() {
        var functionSig = "T requireNonNull2<T>(T|null value, java.util.function.Supplier<string> messageSupplier)";
        var supplierKlass = TestUtils.newKlassBuilder("java.util.function.Supplier").build();
        supplierKlass.setTypeParameters(List.of(new TypeVariable(supplierKlass.nextChildId(), "T", DummyGenericDeclaration.INSTANCE)));
        var func = new TypeParserImpl((String name) -> {
            if(name.equals(supplierKlass.getQualifiedName()))
                return supplierKlass;
            else
                throw new NullPointerException("No such class: " + name);
        }).parseFunction(functionSig, name -> TestUtils.nextRootId());
        Assert.assertEquals("requireNonNull2", func.getName());
        Assert.assertEquals(1, func.getTypeParameters().size());
        var variableType = func.getTypeParameters().getFirst().getType();
        Assert.assertEquals(variableType, func.getReturnType());
        Assert.assertEquals(2, func.getParameters().size());
        Assert.assertEquals("value", func.getParameter(0).getName());
        Assert.assertEquals(new UnionType(Set.of(variableType, Types.getNullType())), func.getParameter(0).getType());
        Assert.assertEquals("messageSupplier", func.getParameter(1).getName());
        Assert.assertEquals(KlassType.create(supplierKlass, List.of(Types.getStringType())), func.getParameter(1).getType());
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

    public void testVariableType() {
        var klass = TestUtils.newKlassBuilder("Foo").build();
        klass.setTypeParameters(List.of(new TypeVariable(klass.nextChildId(), "T", klass)));
        MethodBuilder.newBuilder(klass, "test1")
                .build();
        var m2 = MethodBuilder.newBuilder(klass, "test2").build();
        m2.setTypeParameters(List.of(new TypeVariable(klass.nextChildId(), "E", m2)));
        var pKlass = KlassType.create(klass, List.of(Types.getLongType()));
        var pM2 = pKlass.getMethod(m2);
        var ppM2 = pM2.getParameterized(List.of(Types.getStringType()));
        Assert.assertEquals(pKlass, ppM2.getDeclaringType());
        Assert.assertSame(ppM2.getRawFlow(), m2);
        Assert.assertEquals(List.of(Types.getStringType()), ppM2.getTypeArguments());


        var tv = pM2.getTypeParameters().getFirst().getType();
        var expr = tv.toExpression();
        var map = getEntityMap(klass);
        var typeDefProvider = (TypeDefProvider) id -> (ITypeDef) map.get(id);
        var parser = new TypeParserImpl(typeDefProvider);
        var tv2 = parser.parseType(expr);
        Assert.assertEquals(tv, tv2);

        var typeKey = tv.toTypeKey();
        var tv3 = typeKey.toType(typeDefProvider);
        Assert.assertEquals(tv, tv3);

        var expr1 = typeKey.toTypeExpression();
        Assert.assertEquals(expr, expr1);
    }

    public void testFunctionRef() {
        var func = FunctionBuilder.newBuilder(TestUtils.nextRootId(), "test").build();
        func.setTypeParameters(List.of(
                new TypeVariable(func.nextChildId(), "T", DummyGenericDeclaration.INSTANCE)
        ));
        var map = getEntityMap(func);
        map.put(StdKlass.string.get().getId(), StdKlass.string.get());

        var pFunc = new FunctionRef(func, List.of(Types.getStringType()));
        Assert.assertEquals(1, pFunc.getRawFlow().getTypeParameters().size());
        Assert.assertEquals(List.of(Types.getStringType()), pFunc.getTypeArguments());
        Assert.assertTrue(pFunc.isParameterized());
        var expr = pFunc.toExpression(null);

        TypeDefProvider typeDefProvider = (Id id) -> (ITypeDef) map.get(id);
        var parser = new TypeParserImpl(typeDefProvider);
        var pFuncRef1 = parser.parseFunctionRef(expr);
        Assert.assertEquals(pFunc, pFuncRef1);
    }

    private Map<Id, Entity> getEntityMap(Instance root) {
        var map = new HashMap<Id, Entity>();
        root.forEachDescendant(e -> {
            if(e instanceof Entity entity && entity.tryGetId() != null)
                map.put(entity.getId(), entity);
        });
        return map;
    }

    public void testParType() {
        var type = new ArrayType(new UnionType(Set.of(Types.getNullType(), Types.getStringType())), ArrayKind.DEFAULT);
        var expr = type.toExpression();
        TypeDefProvider typeDefProvider = (Id id) -> {
            if (StdKlass.string.get().idEquals(id)) return StdKlass.string.get();
            throw new UnsupportedOperationException();
        };
        var parser = new TypeParserImpl(typeDefProvider);
        var parsedType = parser.parseType(expr);
        Assert.assertEquals(type, parsedType);
    }

    public void testUnionType() {
        TypeDefProvider typeDefProvider = (Id id) -> {throw new UnsupportedOperationException();};
        var parser = new TypeParserImpl(typeDefProvider);
        var type = (UnionType) parser.parseType("null|string|long");
        Assert.assertEquals(3, type.getMembers().size());
    }

    public void testIntersectionType() {
        TypeDefProvider typeDefProvider = (Id id) -> {throw new UnsupportedOperationException();};
        var parser = new TypeParserImpl(typeDefProvider);
        var type = (IntersectionType) parser.parseType("null&string&long");
        Assert.assertEquals(3, type.getTypes().size());
    }

}