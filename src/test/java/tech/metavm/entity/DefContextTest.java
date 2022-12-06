package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

public class DefContextTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefContextTest.class);

    private DefContext defContext;
    private MockModelInstanceMap modelInstanceMap;

    @Override
    protected void setUp() {
        defContext = new DefContext(o -> null);
        modelInstanceMap = new MockModelInstanceMap(defContext);
    }

    private EntityDef<Type> getTypeDef() {
        return defContext.getEntityDef(Type.class);
    }

    public void testType() {
        TestUtils.logJSON(LOGGER, "type", getTypeDef().getType().toDTO());
    }

    public void testConvertToInstance() {
        EntityDef<Type> typeDef = defContext.getEntityDef(Type.class);
        Type type = typeDef.getType();
        Instance instance = typeDef.createInstance(type, modelInstanceMap);
        InstanceDTO instanceDTO = instance.toDTO();
        TestUtils.logJSON(LOGGER, "instance", instanceDTO);
    }

    public void testConvertFoo() {
        EntityDef<Foo> fooDef = defContext.getEntityDef(Foo.class);
        Foo foo = new Foo("傻不拉几", new Bar("呆头呆脑"));
        Instance instance = fooDef.createInstance(foo, modelInstanceMap);
        Foo recoveredFoo = fooDef.createModel(instance, modelInstanceMap);
        Assert.assertFalse(EntityUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testConvertTypeToEntity() {
        EntityDef<Type> typeDef = defContext.getEntityDef(Type.class);
        Type type = typeDef.getType();
        Instance instance = typeDef.createInstance(type, modelInstanceMap);
        Type recoveredType = typeDef.createModel(instance, modelInstanceMap);
        TypeDTO typeDTO = type.toDTO();
        LOGGER.info(recoveredType.getFields().size() + "");
        TestUtils.logJSON(LOGGER, "typeDTO", typeDTO);
    }

    public void testConvertType() {
        EntityDef<Type> typeDef = defContext.getEntityDef(Type.class);
        Type testType = new Type(
                "Test Type", StandardTypes.OBJECT, TypeCategory.VALUE
        );

        new Field(
                "title", testType, Access.GLOBAL, false, true, null,
                StandardTypes.STRING, false
        );

        Instance instance = typeDef.createInstance(testType, defContext);
        TestUtils.logJSON(LOGGER, instance.toDTO());
    }

    public void testGenerateInstances() {
        defContext = new DefContext(o -> null, new MemInstanceContext());
        ModelDef<Type, ?> typeDef = defContext.getDef(Type.class);
        Assert.assertNotNull(typeDef);

        defContext.finish();

        Assert.assertNotNull(typeDef.getType().getId());

        Table<ConstraintRT<?>> constraints = typeDef.getType().getDeclaredConstraints();
        Assert.assertNotNull(constraints.getId());
    }



}