package tech.metavm.entity;

import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.util.NncUtils;

import java.util.Map;
import java.util.Set;

@Component
public class EntityTypeRegistry implements InitializingBean {

//    private static final Map<Long, Class<?>> CLASS_MAP = new HashMap<>();
//    private static final Map<Class<?>, Long> CLASS_TO_TYPE_ID = new HashMap<>();
//    private static final Map<Long, TypePO> TYPE_MAP = new HashMap<>();
//    private static final Map<Long, EntityDef<?>> DEF_MAP = new HashMap<>();
//    private static final Map<Long, EnumDef> ENUM_DEF_MAP = new HashMap<>();
//    private static final Map<IndexDef<?>, ConstraintPO> uniqueConstraintDef2id = new IdentityHashMap<>();

    @Value("${metavm.id.file}")
    private Map<String, String> idFileMap;

    private final InstanceContextFactory instanceContextFactory;

    private static DefContext DEF_CONTEXT;

    public EntityTypeRegistry(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public static DefContext getDefContext() {
        return DEF_CONTEXT;
    }

    @Override
    public void afterPropertiesSet() {
        Reflections reflections = new Reflections("tech.metavm");
        StdAllocators stdAllocators = new StdAllocators(idFileMap);
        InstanceContext instanceContext = newContext();
        DEF_CONTEXT = new DefContext(
                jc -> NncUtils.get(stdAllocators.getId(jc), instanceContext::get),
                instanceContext.getEntityContext()
        );
        Set<Class<? extends Entity>> entityTypes = reflections.getSubTypesOf(Entity.class);
        for (Class<? extends Entity> entityType : entityTypes) {
            /*EntityDef<?> entityDef = */
            DEF_CONTEXT.getEntityDef(entityType);
//            DEF_MAP.put(entityDef.getTypeId(), entityDef);
//            CLASS_MAP.put(entityDef.getTypeId(), entityType);
        }
    }

    public static Entity createEntity(Instance instance, ModelMap modelMap) {
        EntityDef<?> entityDef = (EntityDef<?>) DEF_CONTEXT.getDef(instance.getType().id);
        return entityDef.newModel(instance, modelMap);
    }

    public static Instance createInstance(Object entity, InstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(entity.getClass());
        return entityDef.newInstanceHelper(entity, instanceMap);
    }

    private static <T extends Entity> Instance newInstanceHelper(EntityDef<T> entityDef, Entity entity, InstanceMap instanceMap) {
        return entityDef.newInstance(entityDef.getEntityType().cast(entity), instanceMap);
    }

    public static void updateInstance(Object model, IInstance instance, InstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(instance.getType().id);
        updateInstanceHelper(entityDef, model, instance, instanceMap);
    }

    private static <T, I extends Instance> void updateInstanceHelper(
            ModelDef<T, I> modelDef,
            Object entity,
            IInstance instance,
            InstanceMap instanceMap) {
        modelDef.updateInstance(
                modelDef.getEntityType().cast(entity),
                modelDef.getInstanceType().cast(instance),
                instanceMap
        );
    }

    public static Type getType(Entity entity) {
        return getType(EntityUtils.getRealType(entity.getClass()));
    }

    public static Type getType(Class<?> entityClass) {
        return DEF_CONTEXT.getDef(entityClass).getType();
    }

    public static ConstraintPO getUniqueConstraint(IndexDef<?> def) {
        return null; // TODO to implement
    }

    public static Class<? extends Entity> getEntityType(long typeId) {
        return DEF_CONTEXT.getEntityDef(typeId).getEntityType();
    }

    public static Class<? extends Enum<?>> getEnumType(long typeId) {
        return DEF_CONTEXT.getEnumDef(typeId).getEnumType();
    }

    public static <T extends Enum<?>> T getEnumConstant(Class<T> enumType, long id) {
        return enumType.cast(DEF_CONTEXT.getEnumDef(enumType).getEnumConstantDef(id).getValue());
    }

    public static EntityDef<?> getEntityDef(long typeId) {
        return DEF_CONTEXT.getEntityDef(typeId);
    }

    public InstanceContext newContext() {
        return instanceContextFactory.newContext();
    }

//    public static Enum<?> getEnumConstant(long id) {
//        return stdAllocators.getEnumConstantById(id);
//    }

}
