package tech.metavm.entity;

import junit.framework.TestCase;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.TestUtils;

public class DefContextTest extends TestCase {

    private final ModelMap modelMap = new MockModelMap(this::createModel);
    private final DefContext context = new DefContext(o -> null, modelMap);
    private final InstanceMap instanceMap = new MockInstanceMap(context::getDef);

    private Object createModel(Instance instance) {
        return context.getDef(instance.getType()).newModelHelper(instance, modelMap);
    }

    private EntityDef<Type> getTypeDef() {
        return context.getEntityDef(Type.class);
    }

    public void testType() {
        TestUtils.printJSON(getTypeDef().getType().toDTO());
    }

    public void testConvertToInstance() {
        DefContext context = new DefContext(o -> null, modelMap);
        EntityDef<Type> typeDef = context.getEntityDef(Type.class);
        IInstance instance = typeDef.newInstance(typeDef.getType(), instanceMap);
        InstanceDTO instanceDTO = instance.toDTO();
        TestUtils.printJSON(instanceDTO);
    }

    public void testConvertToEntity() {
        EntityDef<Type> typeDef = context.getEntityDef(Type.class);
        Instance instance = typeDef.newInstance(typeDef.getType(), instanceMap);
        Type type = typeDef.newModel(instance, modelMap);
        TypeDTO typeDTO = type.toDTO();
        TestUtils.printJSON(typeDTO);
    }

}