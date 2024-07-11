package org.metavm.entity;

import org.metavm.event.EventQueue;
import org.metavm.flow.*;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.*;
import org.metavm.object.view.Mapping;
import org.metavm.object.view.MappingProvider;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.NncUtils;
import org.metavm.util.TypeReference;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.List;

public interface IEntityContext extends Closeable, EntityRepository, TypeProvider, MappingProvider, TypeDefProvider {

    boolean containsEntity(Object entity);

//    default <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
//        return getEntity(typeReference.getType(), id);
//    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, Id id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, String id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T> T getEntity(Class<T> klass, DurableInstance instance) {
        return getEntity(klass, instance, null);
    }

    ObjectInstanceMap getObjectInstanceMap();

    <T> T getEntity(Class<T> klass, DurableInstance instance, @Nullable Mapper<T, ?> mapper);

    DurableInstance getInstance(Object object);

    void invalidateCache(Id id);

    Profiler getProfiler();

    DefContext getDefContext();

//    default <T> void getAllByType(Class<T> klass, List<? super T> result) {
//        T start = null;
//        List<T> batch;
//        do {
//            batch = getByType(klass, start, Constants.BATCH_SIZE);
//            result.addAll(batch);
//            if (!batch.isEmpty())
//                start = batch.get(batch.size() - 1);
//        } while (batch.size() == Constants.BATCH_SIZE);
//    }

//    <T> List<T> getByType(Class<? extends T> type, @Nullable T startExclusive, long limit);

    default List<Klass> getTemplateInstances(Klass template) {
        NncUtils.requireTrue(template.isTemplate());
        return selectByKey(Klass.TEMPLATE_IDX, template);
    }

    <T> List<T> getAllBufferedEntities(Class<T> entityClass);

    void close();

//    boolean existsInstances(Class<?> type);

    boolean containsEntity(Class<?> entityType, Id id);

    //    <T> T getEntity(Class<T> entityType, long id);

    <T> T getBufferedEntity(Class<T> entityType, Id id);

    <T> T getEntity(Class<T> entityType, Id id);

    default <T> T getEntity(Class<T> entityType, String id) {
        return id != null ? getEntity(entityType, Id.parse(id)) : null;
    }

    Type getType(Class<?> javaType);

    default Type getType(String id) {
        return getEntity(Type.class, id);
    }

    @Nullable
    IEntityContext getParent();

    <T> T createEntity(DurableInstance instance, Mapper<T, ?> mapper);

    boolean isNewEntity(Object entity);

    <T> T getRemoved(Class<T> entityClass, Id id);

    boolean isPersisted(Object entity);

//    default Type getType(long id) {
//        return getEntity(Type.class, id);
//    }

    default Type getType(Id id) {
        return getEntity(Type.class, id);
    }

//    default ClassType getClassType(long id) {
//        return getEntity(ClassType.class, id);
//    }

    default TypeDef getTypeDef(Id id) {
        return getEntity(TypeDef.class, id);
    }

    default TypeDef getTypeDef(String id) {
        return getEntity(TypeDef.class, id);
    }

    default Klass getKlass(Id id) {
        return getEntity(Klass.class, id);
    }

    default Klass getKlass(String id) {
        return getEntity(Klass.class, id);
    }

    @Nullable
    EventQueue getEventQueue();

    long getAppId(Object model);

    Instance resolveInstance(Object value);

    long getAppId();

//    default Field getField(long id) {
//        return getEntity(Field.class, id);
//    }

    default Field getField(Id id) {
        return getEntity(Field.class, id);
    }

    default Field getField(String id) {
        return getEntity(Field.class, id);
    }

    default TypeVariable getTypeVariable(Id id) {
        return getEntity(TypeVariable.class, id);
    }

    default TypeVariable getTypeVariable(String id) {
        return getEntity(TypeVariable.class, id);
    }

    default CapturedTypeVariable getCapturedTypeVariable(String id) {
        return getEntity(CapturedTypeVariable.class, id);
    }

    default NodeRT getNode(Id id) {
        return getEntity(NodeRT.class, id);
    }

    default NodeRT getNode(String id) {
        return getEntity(NodeRT.class, id);
    }

    default Flow getFlow(Id id) {
        return getEntity(Flow.class, id);
    }

    default Flow getFlow(String id) {
        return getEntity(Flow.class, id);
    }

    default ScopeRT getScope(Id id) {
        return getEntity(ScopeRT.class, id);
    }

    default ScopeRT getScope(String id) {
        return getEntity(ScopeRT.class, id);
    }

    default Method getMethod(Id id) {
        return getEntity(Method.class, id);
    }

    default Method getMethod(String id) {
        return getEntity(Method.class, id);
    }

    default ObjectMapping getObjectMapping(Id id) {
        return getEntity(ObjectMapping.class, id);
    }

    default ObjectMapping getObjectMapping(String id) {
        return getEntity(ObjectMapping.class, id);
    }

    default Mapping getMapping(Id id) {
        return getEntity(Mapping.class, id);
    }

    default Mapping getMapping(String id) {
        return getEntity(Mapping.class, id);
    }

    default Function getFunction(Id id) {
        return getEntity(Function.class, id);
    }

    default Function getFunction(String id) {
        return getEntity(Function.class, id);
    }

    boolean isFinished();

    void finish();

    IInstanceContext getInstanceContext();

    <T> List<T> query(EntityIndexQuery<T> query);

    long count(EntityIndexQuery<?> query);

    void updateInstances();

    void update(Object object);

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... refValues);

    boolean remove(Object model);

    void batchRemove(List<?> entities);

    @Nullable
    default <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object... values) {
        return NncUtils.first(selectByKey(indexDef, values));
    }

    void initIds();

    default boolean tryBind(Object entity) {
        if (isBindSupported() && isNewEntity(entity)) {
            bind(entity);
            return true;
        } else
            return false;
    }

    <T> T bind(T entity);

    default boolean isBindSupported() {
        return true;
    }

    void initIdManually(Object model, Id id);

    boolean isRemoved(Object entity);

    IEntityContext createSame(long appId);

    List<Object> scan(long start, long limit);

}
