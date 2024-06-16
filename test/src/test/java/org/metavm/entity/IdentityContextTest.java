package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.api.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.*;

import java.util.IdentityHashMap;
import java.util.List;

public class IdentityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(IdentityContextTest.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockStandardTypesInitializer.init();
    }

    public void test() {
        IdentityContext identityContext = new IdentityContext();
        var fooKlass = KlassBuilder.newBuilder("Foo", "org.metavm.Foo")
                .source(ClassSource.BUILTIN)
                .build();

        var fooNameField = FieldBuilder.newBuilder("name", "name", fooKlass, StandardTypes.getStringType())
                .build();

        var typeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);
        var method = MethodBuilder.newBuilder(fooKlass, "bar", "bar")
                .returnType(StandardTypes.getVoidType())
                .typeParameters(List.of(typeVar))
                .parameters(new Parameter(null, "t", "t", typeVar.getType()))
                .type(new FunctionType(List.of(typeVar.getType()), StandardTypes.getVoidType()))
                .staticType(new FunctionType(List.of(fooKlass.getType(), typeVar.getType()), StandardTypes.getVoidType()))
                .build();

        var identities = new IdentityHashMap<Object, ModelIdentity>();
        EntityUtils.visitGraph(List.of(fooKlass),
                entity -> {
                    if (!(entity instanceof Instance) && !(entity instanceof Value))
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