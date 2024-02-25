package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.expression.ConstantExpression;
import tech.metavm.flow.NodeRT;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefContextTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefContextTest.class);

    private DefContext defContext;
    private ObjectInstanceMap objectInstanceMap;
    private TypeFactory typeFactory;
    private MockIdProvider idProvider;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        var bridge = new EntityInstanceContextBridge();
        var instanceContext = InstanceContextBuilder.newBuilder(Constants.ROOT_APP_ID,
                        new MemInstanceStore(), new DefaultIdInitializer(idProvider), bridge, bridge, bridge)
                .readonly(false)
                .build();
        defContext = new DefContext(
                new StdIdProvider(new EmptyStdIdStore()), instanceContext, new MemColumnStore(), new IdentityContext());
        bridge.setEntityContext(defContext);
        objectInstanceMap = defContext.getObjectInstanceMap();
        typeFactory = new DefaultTypeFactory(defContext::getType);
    }

    public void testGetDef() {
//        ClassType fooType = defContext.getClassType(Foo.class);
        Field field = defContext.getField(ClassType.class, "fields");
        Assert.assertFalse(field.isUnique());
    }

    public void testConvertToInstance() {
        EntityDef<ClassType> typeDef = defContext.getEntityDef(ClassType.class);
        ClassType type = typeDef.getType();
        var instance = (DurableInstance) objectInstanceMap.getInstance(type);
//                typeDef.createInstance(type, objectInstanceMap, null);
//        InstanceDTO instanceDTO = instance.toDTO();
//        TestUtils.logJSON(LOGGER, "instance", instanceDTO);
        Assert.assertEquals(type.tryGetId(), instance.tryGetPhysicalId());
    }

    public void testConvertFoo() {
        EntityDef<Foo> fooDef = defContext.getEntityDef(Foo.class);
        Foo foo = new Foo(" Big Foo", new Bar("Bar001"));
        ClassInstance instance = (ClassInstance) objectInstanceMap.getInstance(foo);
        Foo recoveredFoo = fooDef.createModel(instance, objectInstanceMap);
        Assert.assertFalse(DiffUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testConvertType() {
        EntityDef<ClassType> typeDef = defContext.getEntityDef(ClassType.class);
        ClassType type = typeDef.getType();
        var instance = (ClassInstance) objectInstanceMap.getInstance(type);
        typeDef.initInstance(instance, type, objectInstanceMap);
        ClassType recoveredType = typeDef.createModel(instance, objectInstanceMap);
        MatcherAssert.assertThat(recoveredType, PojoMatcher.of(type));
    }

    public void testGenerateInstances() {

    }

    public void testInheritance() {
        EntityDef<NodeRT> superDef = defContext.getEntityDef(new TypeReference<>() {
        });
        Assert.assertSame(Objects.requireNonNull(superDef.getType().getSuperClass()).getSuperClass(), typeFactory.getEntityType());
    }

    public void testArrayFieldType() {
        Field bazListField = defContext.getField(Foo.class, "bazList");
        Type fieldType = bazListField.getType();
        Assert.assertTrue(fieldType instanceof UnionType);
        Assert.assertTrue(fieldType.getUnderlyingType() instanceof ArrayType);
        ArrayType arrayType = (ArrayType) fieldType.getUnderlyingType();
        Assert.assertSame(defContext.getType(Baz.class), arrayType.getElementType());
    }

    public void test_field_with_instance_type() {
        PojoDef<ConstantExpression> def = defContext.getPojoDef(ConstantExpression.class);
        ClassType quxType = defContext.getClassType(Qux.class);
        quxType.initId(1000000L);
        Field quxAmountField = defContext.getField(Qux.class, "amount");
        var qux = ClassInstance.create(
                Map.of(quxAmountField, Instances.longInstance(100L)),
                quxType
        );
        qux.initId(PhysicalId.of(idProvider.allocateOne(TestConstants.APP_ID, quxType)));
        ConstantExpression model = new ConstantExpression(qux);
        Instance instance = def.createInstance(model, objectInstanceMap, null);
        ConstantExpression recoveredModel = def.createModelHelper(instance, objectInstanceMap);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void testGetIdentityMap() {
        PojoDef<Type> def = defContext.getPojoDef(Type.class);
        ClassType type = def.getType();
        Map<Object, ModelIdentity> identityMap = defContext.getIdentityMap();
        Set<ModelAndPath> models = EntityUtils.getReachableObjects(
                List.of(type), o -> o instanceof Entity || o instanceof Enum<?>, true
        );
        defContext.flush();
        for (ModelAndPath modelAndPath : models) {
            Object model = modelAndPath.model();
            String path = modelAndPath.path();
            if ((model instanceof Identifiable identifiable) && !EntityUtils.isEphemeral(model)) {
                ModelIdentity identity = identityMap.get(identifiable);
                Assert.assertNotNull(
                        "Can not find identity for model '" + model + "' at path '" + path + "'",
                        identity
                );
            }
        }
    }

}