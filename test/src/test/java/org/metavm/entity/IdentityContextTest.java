package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.List;

public class IdentityContextTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(IdentityContextTest.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockStandardTypesInitializer.init();
    }

    public void test() {
        IdentityContext identityContext = new IdentityContext();
        var fooKlass = TestUtils.newKlassBuilder("Foo", "org.metavm.Foo")
                .source(ClassSource.BUILTIN)
                .build();

        var fooNameField = FieldBuilder.newBuilder("name", "name", fooKlass, Types.getStringType())
                .build();

        var typeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);
        var method = MethodBuilder.newBuilder(fooKlass, "bar", "bar")
                .returnType(Types.getVoidType())
                .typeParameters(List.of(typeVar))
                .parameters(new Parameter(null, "t", "t", typeVar.getType()))
                .type(new FunctionType(List.of(typeVar.getType()), Types.getVoidType()))
                .staticType(new FunctionType(List.of(fooKlass.getType(), typeVar.getType()), Types.getVoidType()))
                .build();

        var identities = new IdentityHashMap<Object, ModelIdentity>();
        EntityUtils.visitGraph(List.of(fooKlass),
                entity -> {
                    if (!(entity instanceof Value) && !(entity instanceof org.metavm.api.Value))
                        identities.put(entity, identityContext.getModelId(entity));
                }
        );
        Assert.assertTrue(identities.containsKey(fooKlass));
        Assert.assertTrue(identities.containsKey(fooNameField));
        Assert.assertTrue(identities.containsKey(method));
        Assert.assertTrue(identities.containsKey(method.getParameters().get(0)));
        Assert.assertTrue(identities.containsKey(method.getRootScope()));
    }

}