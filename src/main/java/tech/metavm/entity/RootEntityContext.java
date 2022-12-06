//package tech.metavm.entity;
//
//import tech.metavm.object.instance.Instance;
//import tech.metavm.object.instance.ModelInstanceMap;
//import tech.metavm.object.meta.StandardTypes;
//import tech.metavm.util.TypeReference;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class RootEntityContext implements IEntityContext, ModelInstanceMap {
//
//    private final Map<Long, Entity> entityMap = new HashMap<>();
//    private final Map<Object, Instance> model2instance = new HashMap<>();
//    private final Map<Instance, Object> instance2model = new HashMap<>();
//    private final RootInstanceContext instanceContext;
////    private final DefContext defContext;
//
//    RootEntityContext(RootInstanceContext instanceContext) {
//        this.instanceContext = instanceContext;
////        defContext = new DefContext(this::getInstanceByModel, this);
//        init();
//    }
//
//    public void init() {
//        for (Entity entity : StandardTypes.entities()) {
//            entityMap.put(entity.getId(), entity);
//            ModelDef<?,?> def = RootRegistry.getDef(entity.getClass());
//            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType());
//            instance.initId(entity.getId());
//            instanceContext.addInstance(instance);
//            model2instance.put(entity, instance);
//            instance2model.put(instance, entity);
//            def.initInstanceHelper(instance, entity, this);
//        }
//    }
//
//    @Override
//    public Instance getInstance(Object model) {
//        return model2instance.get(model);
//    }
//
//    @Override
//    public <T> T getModel(Class<T> klass, Instance instance) {
//        return klass.cast(model2instance.get(instance));
//    }
//
//    @Override
//    public boolean containsInstance(Instance instance) {
//        return instance2model.containsKey(instance);
//    }
//
//    public boolean containsKey(EntityKey entityKey) {
//        Entity entity = entityMap.get(entityKey.id());
//        return entityKey.type().isInstance(entity);
//    }
//
//    @Override
//    public boolean containsModel(Object model) {
//        return model2instance.containsKey(model);
//    }
//
//    public <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
//        return typeReference.getType().cast(entityMap.get(id));
//    }
//
//    public <T extends Entity> T getEntity(Class<T> entityType, long id) {
//        return entityType.cast(entityMap.get(id));
//    }
//
//    @Override
//    public void finish() {
//
//    }
//
//}
