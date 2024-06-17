package org.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.api.Value;
import org.metavm.expression.ConstantExpression;
import org.metavm.flow.NodeRT;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Baz;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefContextTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(DefContextTest.class);

    private DefContext defContext;
    private ObjectInstanceMap objectInstanceMap;
    private MockIdProvider idProvider;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        var bridge = new EntityInstanceContextBridge();
        var instanceContext = InstanceContextBuilder.newBuilder(Constants.ROOT_APP_ID,
                        new MemInstanceStore(), new DefaultIdInitializer(idProvider), bridge, bridge)
                .readonly(false)
                .build();
        defContext = new DefContext(
                new StdIdProvider(new EmptyStdIdStore()), instanceContext, new MemColumnStore(), new MemTypeTagStore(), new IdentityContext());
        bridge.setEntityContext(defContext);
        objectInstanceMap = defContext.getObjectInstanceMap();
        defContext.postProcess();
    }

    public void testGetDef() {
//        ClassType fooType = defContext.getClassType(Foo.class);
        Field field = defContext.getField(Klass.class, "fields");
        Assert.assertFalse(field.isUnique());
    }

    public void testConvertToInstance() {
        EntityDef<Klass> typeDef = defContext.getEntityDef(Klass.class);
        Klass type = typeDef.getKlass();
        var instance = (DurableInstance) objectInstanceMap.getInstance(type);
//                typeDef.createInstance(type, objectInstanceMap, null);
//        InstanceDTO instanceDTO = instance.toDTO();
//        TestUtils.logJSON(LOGGER, "instance", instanceDTO);
        Assert.assertEquals(type.tryGetId(), instance.tryGetId());
    }

    public void testConvertFoo() {
        EntityDef<Foo> fooDef = defContext.getEntityDef(Foo.class);
        Foo foo = new Foo(" Big Foo", new Bar("Bar001"));
        ClassInstance instance = (ClassInstance) objectInstanceMap.getInstance(foo);
        Foo recoveredFoo = fooDef.createEntity(instance, objectInstanceMap);
        Assert.assertFalse(DiffUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testConvertType() {
        EntityDef<Klass> typeDef = defContext.getEntityDef(Klass.class);
        Klass type = typeDef.getKlass();
        var instance = (ClassInstance) objectInstanceMap.getInstance(type);
        typeDef.initInstance(instance, type, objectInstanceMap);
        Klass recoveredType = typeDef.createEntity(instance, objectInstanceMap);
        MatcherAssert.assertThat(recoveredType, PojoMatcher.of(type));
    }

    public void testGenerateInstances() {

    }

    public void testInheritance() {
        EntityDef<NodeRT> superDef = defContext.getEntityDef(new TypeReference<>() {
        });
        Assert.assertEquals(Objects.requireNonNull(superDef.getKlass().getSuperType()).getSuperType(), BuiltinKlasses.entity.get().getType());
    }

    public void testArrayFieldType() {
        Field bazListField = defContext.getField(Foo.class, "bazList");
        Type fieldType = bazListField.getType();
        Assert.assertTrue(fieldType instanceof UnionType);
        Assert.assertTrue(fieldType.getUnderlyingType() instanceof ArrayType);
        ArrayType arrayType = (ArrayType) fieldType.getUnderlyingType();
        Assert.assertEquals(defContext.getType(Baz.class), arrayType.getElementType());
    }

    public void test_field_with_instance_type() {
        PojoDef<ConstantExpression> def = defContext.getPojoDef(ConstantExpression.class);
        var quxType = defContext.getClassType(Qux.class).resolve();
        quxType.initId(PhysicalId.of(1000000L, 0L, TestUtils.mockClassType()));
        Field quxAmountField = defContext.getField(Qux.class, "amount");
        var qux = ClassInstance.create(
                Map.of(quxAmountField, Instances.longInstance(100L)),
                quxType.getType()
        );
        qux.initId(PhysicalId.of(idProvider.allocateOne(TestConstants.APP_ID, quxType.getType()), 0L, quxType.getType()));
        ConstantExpression model = new ConstantExpression(qux);
        Instance instance = def.createInstance(model, objectInstanceMap, null);
        ConstantExpression recoveredModel = def.createEntityHelper(instance, objectInstanceMap);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void testGetIdentityMap() {
        PojoDef<Type> def = defContext.getPojoDef(Type.class);
        Klass type = def.getKlass();
        Map<Object, ModelIdentity> identityMap = defContext.getIdentityMap();
        Set<ModelAndPath> models = EntityUtils.getReachableObjects(
                List.of(type), o -> o instanceof Entity || o instanceof Enum<?>, true
        );
        defContext.flush();
        for (ModelAndPath modelAndPath : models) {
            Object model = modelAndPath.model();
            String path = modelAndPath.path();
            if (!(model instanceof Value) && model instanceof Identifiable identifiable && !EntityUtils.isEphemeral(model)) {
                ModelIdentity identity = identityMap.get(identifiable);
                Assert.assertNotNull(
                        "Can not find identity for model '" + model + "' at path '" + path + "'",
                        identity
                );
            }
        }
    }

}