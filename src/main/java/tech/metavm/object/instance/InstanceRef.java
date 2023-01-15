//package tech.metavm.object.instance;
//
//import org.jetbrains.annotations.Nullable;
//import tech.metavm.entity.Entity;
//import tech.metavm.entity.EntityProxyFactory;
//import tech.metavm.object.instance.persistence.IndexItemPO;
//import tech.metavm.object.instance.rest.InstanceDTO;
//import tech.metavm.object.meta.Field;
//import tech.metavm.object.meta.Type;
//
//import java.util.List;
//import java.util.function.Supplier;
//
//public class InstanceRef implements IInstance {
//
//    private final Long id;
//    private final Class<?> entityType;
////    private final InstanceContext context;
//    private final Supplier<Instance> instanceLoader;
//    private Instance realInstance;
//
//    public InstanceRef(Long id, Class<?> entityType, Supplier<Instance> instanceLoader) {
//        this.id = id;
//        this.entityType = entityType;
//        this.instanceLoader = instanceLoader;
//    }
//
//    private void ensureLoaded() {
//        if(realInstance != null) {
//           return;
//        }
//        realInstance = instanceLoader.get();
//    }
//
//    public Instance getRealInstance() {
//        ensureLoaded();
//        return realInstance;
//    }
//
//    @Override
//    public Long getId() {
//        return id;
//    }
//
////    public long getTenantId() {
////        return context.getTenantId();
////    }
//
////    public InstanceContext getContext() {
////        return context;
////    }
//
//    @Override
//    public String getTitle() {
//        ensureLoaded();
//        return realInstance.getTitle();
//    }
//
//    @Override
//    public void update(InstanceDTO instanceDTO) {
//        ensureLoaded();
//        realInstance.update(instanceDTO);
//    }
//
//    @Override
//    public Type getType() {
//        ensureLoaded();
//        return realInstance.getType();
//    }
//
//    @Override
//    public IInstance getInstance(Field indexItem) {
//        ensureLoaded();
//        return realInstance.getInstance(indexItem);
//    }
//
//    @Override
//    public Object getRaw(String fieldName) {
//        ensureLoaded();
//        return realInstance.getRaw(fieldName);
//    }
//
//    @Override
//    public Object getRaw(Field indexItem) {
//        ensureLoaded();
//        return realInstance.getRaw(indexItem);
//    }
//
//    @Override
//    public Object getRaw(long fieldId) {
//        ensureLoaded();
//        return realInstance.getRaw(fieldId);
//    }
//
//    @Override
//    public String getString(Field indexItem) {
//        ensureLoaded();
//        return realInstance.getString(indexItem);
//    }
//
//    @Override
//    public void remove() {
//        ensureLoaded();
//        realInstance.remove();
//    }
//
//    @Override
//    public InstanceDTO toDTO() {
//        ensureLoaded();
//        return realInstance.toDTO();
//    }
//
//    @Override
//    public void set(Field indexItem, Object value) {
//        ensureLoaded();
//        realInstance.set(indexItem, value);
//    }
//
//    @Override
//    public Object getResolved(List<Long> fieldPath) {
//        ensureLoaded();
//        return realInstance.getRaw(fieldPath);
//    }
//
//    @Override
//    public Object getRaw(List<Long> fieldPath) {
//        ensureLoaded();
//        return realInstance.getRaw(fieldPath);
//    }
//
//    @Override
//    public List<IndexItemPO> getUniqueKeys() {
//        ensureLoaded();
//        return realInstance.getUniqueKeys();
//    }
//
//    @Nullable
//    @Override
//    public Class<?> getEntityType() {
//        return entityType;
//    }
//
//    @Override
//    public InstanceArray getInstanceArray(Field indexItem) {
//        ensureLoaded();
//        return realInstance.getInstanceArray(indexItem);
//    }
//
////    public <T extends Entity> T createEntityRef(Class<T> klass) {
////        return EntityProxyFactory.getProxyInstance(klass, this);
////    }
//
//}
