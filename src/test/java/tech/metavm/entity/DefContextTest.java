package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.TestUtils;

public class DefContextTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefContextTest.class);

    private final ModelMap modelMap = new MockModelMap(this::createModel);
    private final DefContext context = new DefContext(o -> null, modelMap);
    private final InstanceMap instanceMap = new MockInstanceMap(context::getDef);

    @Override
    protected void setUp() {
        new StandardDefBuilder().initRootTypes(context);
    }

    private Object createModel(Instance instance) {
        return context.getDef(instance.getType()).newModelHelper(instance, modelMap);
    }

    private EntityDef<Type> getTypeDef() {
        return context.getEntityDef(Type.class);
    }

    public void testType() {
        TestUtils.logJSON(LOGGER, "type", getTypeDef().getType().toDTO());
    }

    public void testConvertToInstance() {
        EntityDef<Type> typeDef = context.getEntityDef(Type.class);
        Type type = typeDef.getType();
        IInstance instance = typeDef.newInstance(type, instanceMap);
        InstanceDTO instanceDTO = instance.toDTO();
        TestUtils.logJSON(LOGGER, "instance", instanceDTO);
    }

    public void testConvertToEntity() {
        EntityDef<Type> typeDef = context.getEntityDef(Type.class);
        Type type = typeDef.getType();
        Instance instance = typeDef.newInstance(type, instanceMap);
        Type recoveredType = typeDef.newModel(instance, modelMap);
        TypeDTO typeDTO = type.toDTO();
        TestUtils.logJSON(LOGGER, "typeDTO", typeDTO);
        Assert.assertFalse(EntityUtils.isPojoDifferent(type, recoveredType));
    }

}