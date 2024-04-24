package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;

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
        var fooType = ClassTypeBuilder.newBuilder("Foo", "tech.metavm.Foo")
                .source(ClassSource.BUILTIN)
                .build();

        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, StandardTypes.getStringType())
                .build();

        var typeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);
        var method = MethodBuilder.newBuilder(fooType, "bar", "bar")
                .returnType(StandardTypes.getVoidType())
                .typeParameters(List.of(typeVar))
                .parameters(new Parameter(null, "t", "t", typeVar.getType()))
                .type(new FunctionType(null, List.of(typeVar.getType()), StandardTypes.getVoidType()))
                .staticType(new FunctionType(null, List.of(fooType.getType(), typeVar.getType()), StandardTypes.getVoidType()))
                .build();

        var identities = new IdentityHashMap<Object, ModelIdentity>();
        EntityUtils.visitGraph(List.of(fooType),
                entity -> {
                    if (!(entity instanceof Instance))
                        identities.put(entity, identityContext.getModelId(entity));
                }
        );
        Assert.assertTrue(identities.containsKey(fooType));
        Assert.assertTrue(identities.containsKey(fooNameField));
        Assert.assertTrue(identities.containsKey(fooNameField.getType()));
        Assert.assertTrue(identities.containsKey(method));
        Assert.assertTrue(identities.containsKey(method.getParameters().get(0)));
        Assert.assertTrue(identities.containsKey(method.getParameters().get(0).getType()));
        Assert.assertTrue(identities.containsKey(method.getType()));
        Assert.assertTrue(identities.containsKey(method.getRootScope()));
        Assert.assertTrue(identities.containsKey(method.getStaticType()));
        Assert.assertTrue(identities.containsKey(method.getReturnType()));
    }

}