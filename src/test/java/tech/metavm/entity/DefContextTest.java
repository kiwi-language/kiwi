package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.GetRelatedNode;
import tech.metavm.flow.NodeRT;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

public class DefContextTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefContextTest.class);

    private DefContext defContext;
    private MockModelInstanceMap modelInstanceMap;
    private TypeFactory typeFactory;

    @Override
    protected void setUp() {
        defContext = new DefContext(o -> null);
        modelInstanceMap = new MockModelInstanceMap(defContext);
        typeFactory = new TypeFactory(defContext::getType);
    }

    private EntityDef<ClassType> getTypeDef() {
        return defContext.getEntityDef(ClassType.class);
    }

    public void testConvertToInstance() {
        EntityDef<ClassType> typeDef = defContext.getEntityDef(ClassType.class);
        ClassType type = typeDef.getType();
        Instance instance = typeDef.createInstance(type, modelInstanceMap);
        InstanceDTO instanceDTO = instance.toDTO();
        TestUtils.logJSON(LOGGER, "instance", instanceDTO);
    }

    public void testConvertFoo() {
        EntityDef<Foo> fooDef = defContext.getEntityDef(Foo.class);
        Foo foo = new Foo("傻不拉几", new Bar("呆头呆脑"));
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

        Table<ConstraintRT<?>> constraints = classType.getDeclaredConstraints();
        Assert.assertNotNull(constraints);
        Assert.assertNotNull(constraints.getId());
    }

    public void testInheritance() {
        EntityDef<GetRelatedNode> def = defContext.getEntityDef(GetRelatedNode.class);
        EntityDef<NodeRT<?>> superDef = defContext.getEntityDef(new TypeReference<>(){});
        Assert.assertSame(superDef.getType(), def.getType().getSuperType());
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

}