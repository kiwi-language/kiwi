package org.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.api.ValueObject;
import org.metavm.expression.ConstantExpression;
import org.metavm.flow.Node;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Baz;
import org.metavm.mocks.Foo;
import org.metavm.mocks.Qux;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.cache.LocalCache;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SystemDefContextTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(SystemDefContextTest.class);

    private SystemDefContext defContext;
    private ObjectInstanceMap objectInstanceMap;
    private MockIdProvider idProvider;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        var bridge = new EntityInstanceContextBridge();
        var instanceContext = InstanceContextBuilder.newBuilder(Constants.ROOT_APP_ID,
                        new MemInstanceStore(new LocalCache()), new DefaultIdInitializer(idProvider), bridge, bridge, bridge)
                .readonly(false)
                .build();
        defContext = new SystemDefContext(
                new StdIdProvider(new EmptyStdIdStore()), instanceContext, new MemColumnStore(), new MemTypeTagStore(), new IdentityContext());
        bridge.setEntityContext(defContext);
        objectInstanceMap = defContext.getObjectInstanceMap();
    }

    public void testGetDef() {
//        ClassType fooType = defContext.getClassType(Foo.class);
        Field field = defContext.getField(Klass.class, "fields");
        Assert.assertFalse(field.isUnique());
    }

    public void testConvertToInstance() {
        EntityDef<Klass> typeDef = defContext.getEntityDef(Klass.class);
        Klass type = typeDef.getKlass();
        var instance = objectInstanceMap.getInstance(type);
        Assert.assertEquals(type.tryGetId(), instance.tryGetId());
    }

    public void testConvertFoo() {
        EntityDef<Foo> fooDef = defContext.getEntityDef(Foo.class);
        Foo foo = new Foo(" Big Foo", new Bar("Bar001"));
        ClassInstance instance = objectInstanceMap.getInstance(foo).resolveObject();
        Foo recoveredFoo = fooDef.createEntity(instance, objectInstanceMap);
        Assert.assertFalse(DiffUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testConvertType() {
        EntityDef<Klass> typeDef = defContext.getEntityDef(Klass.class);
        Klass type = typeDef.getKlass();
        var instance = objectInstanceMap.getInstance(type).resolveObject();
        typeDef.initInstance(instance, type, objectInstanceMap);
        Klass recoveredType = typeDef.createEntity(instance, objectInstanceMap);
        MatcherAssert.assertThat(recoveredType, PojoMatcher.of(type));
    }

    public void testGenerateInstances() {

    }

    public void testInheritance() {
        defContext.postProcess();
        EntityDef<Node> superDef = defContext.getEntityDef(new TypeReference<>() {
        });
        Assert.assertEquals(Objects.requireNonNull(superDef.getKlass().getSuperType()).getSuperType(), StdKlass.entity.get().getType());
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
        ConstantExpression model = new ConstantExpression(qux.getReference());
        var instance = def.createInstance(model, objectInstanceMap, null);
        ConstantExpression recoveredModel = def.createEntityHelper(instance, objectInstanceMap);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void testGetIdentityMap() {
        PojoDef<Type> def = defContext.getPojoDef(Type.class);
//        PojoDef<Type> def = defContext.getPojoDef(Type.class);
        Klass type = def.getKlass();
        Map<Object, ModelIdentity> identityMap = defContext.getIdentityMap();
        Set<ModelAndPath> models = EntityUtils.getReachableObjects(
                List.of(type), o -> o instanceof Entity || o instanceof Enum<?>, true
        );
        defContext.flush();
        for (ModelAndPath modelAndPath : models) {
            Object model = modelAndPath.model();
            String path = modelAndPath.path();
            if (!(model instanceof ValueObject) && model instanceof Identifiable identifiable && !EntityUtils.isEphemeral(model)) {
                ModelIdentity identity = identityMap.get(identifiable);
                Assert.assertNotNull(
                        "Can not find identity for model '" + model + "' at path '" + path + "'",
                        identity
                );
            }
        }
    }

}