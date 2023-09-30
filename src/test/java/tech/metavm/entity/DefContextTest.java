package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.NodeRT;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.expression.ConstantExpression;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefContextTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefContextTest.class);

    private DefContext defContext;
    private MockModelInstanceMap modelInstanceMap;
    private TypeFactory typeFactory;
    private MockIdProvider idProvider;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        defContext = new DefContext(o -> null);
        modelInstanceMap = new MockModelInstanceMap(defContext);
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
        Instance instance = typeDef.createInstance(type, modelInstanceMap);
//        InstanceDTO instanceDTO = instance.toDTO();
//        TestUtils.logJSON(LOGGER, "instance", instanceDTO);
        Assert.assertEquals(type.getId(), instance.getId());
    }

    public void testConvertFoo() {
        EntityDef<Foo> fooDef = defContext.getEntityDef(Foo.class);
        Foo foo = new Foo(" Big Foo", new Bar("Bar001"));
        ClassInstance instance = fooDef.createInstance(foo, modelInstanceMap);
        Foo recoveredFoo = fooDef.createModel(instance, modelInstanceMap);
        Assert.assertFalse(EntityUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testConvertType() {
        EntityDef<ClassType> typeDef = defContext.getEntityDef(ClassType.class);
        ClassType type = typeDef.getType();
        ClassInstance instance = typeDef.createInstance(type, modelInstanceMap);
        ClassType recoveredType = typeDef.createModel(instance, modelInstanceMap);
        MatcherAssert.assertThat(recoveredType, PojoMatcher.of(type));
    }

    public void testGenerateInstances() {
        defContext = new DefContext(o -> null, new MemInstanceContext());
        ModelDef<ClassType, ?> typeDef = defContext.getDef(ClassType.class);
        Assert.assertNotNull(typeDef);

        defContext.finish();

        Assert.assertNotNull(typeDef.getType().getId());

        Assert.assertTrue(typeDef.getType() instanceof ClassType);
        ClassType classType = (ClassType) typeDef.getType();

        Table<Constraint<?>> constraints = classType.getDeclaredConstraints();
        Assert.assertNotNull(constraints);
        Assert.assertNotNull(constraints.getId());
    }

    public void testInheritance() {
        EntityDef<NodeRT<?>> superDef = defContext.getEntityDef(new TypeReference<>(){});
        Assert.assertSame(superDef.getType().getSuperType(), typeFactory.getEntityType());
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
        Field quxAmountField = defContext.getField(Qux.class, "amount");
        Instance qux = new ClassInstance(
                Map.of(
                        quxAmountField, InstanceUtils.longInstance(100L)
                ),
                quxType
        );
        qux.initId(idProvider.allocateOne(TestConstants.TENANT_ID, quxType));

        ConstantExpression model = new ConstantExpression(qux);
        Instance instance = def.createInstance(model, modelInstanceMap);
        ConstantExpression recoveredModel = def.createModelHelper(instance, modelInstanceMap);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void testGetIdentityMap() {
        PojoDef<Type> def = defContext.getPojoDef(Type.class);
        ClassType type = def.getType();
        Map<Object, ModelIdentity> identityMap = defContext.getIdentityMap();
        Set<ModelAndPath> models = ReflectUtils.getReachableObjects(
                List.of(type), o -> !(o instanceof Instance), true
        );
        for (ModelAndPath modelAndPath : models) {
            Object model = modelAndPath.model();
            String path  = modelAndPath.path();
            if((model instanceof Identifiable identifiable)) {
                ModelIdentity identity = identityMap.get(identifiable);
                Assert.assertNotNull(
                        "Can not find identity for model '" + model + "' at path '" + path + "'",
                        identity
                );
            }
        }
    }

}