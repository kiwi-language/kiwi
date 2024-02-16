package tech.metavm.entity;

import tech.metavm.common.RefDTO;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.*;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.*;
import tech.metavm.object.view.FieldsObjectMapping;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IEntityContext extends Closeable, EntityRepository, TypeProvider, MappingProvider {

    boolean containsModel(Object model);

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, RefDTO ref) {
        return getEntity(typeReference.getType(), ref);
    }

    default <T> T getEntity(Class<T> klass, DurableInstance instance) {
        return getEntity(klass, instance, null);
    }

    ObjectInstanceMap getObjectInstanceMap();

    <T> T getEntity(Class<T> klass, DurableInstance instance, @Nullable ModelDef<T, ?> def);

    DurableInstance getInstance(Object object);

    void invalidateCache(long id);

    Profiler getProfiler();

    DefContext getDefContext();

    default <T> void getAllByType(Class<T> klass, List<? super T> result) {
        T start = null;
        List<T> batch;
        do {
            batch = getByType(klass, start, Constants.BATCH_SIZE);
            result.addAll(batch);
            if (!batch.isEmpty())
                start = batch.get(batch.size() - 1);
        } while (batch.size() == Constants.BATCH_SIZE);
    }

    <T> List<T> getByType(Class<? extends T> type, @Nullable T startExclusive, long limit);

    default List<ClassType> getTemplateInstances(ClassType template) {
        NncUtils.requireTrue(template.isTemplate());
        return selectByKey(ClassType.TEMPLATE_IDX,template);
    }

    <T> List<T> getAllBufferedEntities(Class<T> entityClass);

    void close();

    boolean existsInstances(Class<?> type);

    boolean containsEntity(Class<?> entityType, long id);

    ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments);

    default ClassType getParameterizedType(ClassType template, Type...typeArguments) {
        return getParameterizedType(template, List.of(typeArguments));
    }

    FunctionType getFunctionType(List<Type> parameterTypes, Type returnType);

    GenericContext getGenericContext();

    FunctionTypeContext getFunctionTypeContext();

    UncertainType getUncertainType(Type lowerBound, Type upperBound);

    UncertainTypeContext getUncertainTypeContext();

    UnionTypeContext getUnionTypeContext();

    ArrayTypeContext getArrayTypeContext(ArrayKind kind);

    UnionType getNullableType(Type type);

    Set<CompositeType> getNewCompositeTypes();

    <T> T getEntity(Class<T> entityType, long id);

    <T> T getBufferedEntity(Class<T> entityType, long id);

    @Nullable<T> T getEntity(Class<T> entityType, RefDTO ref);

    Type getType(Class<?> javaType);

    @Nullable IEntityContext getParent();

    <T> T createEntity(DurableInstance instance, ModelDef<T, ?> def);

    default FunctionType getFunctionType(RefDTO ref) {
        return getEntity(FunctionType.class, ref);
    }

    boolean isNewEntity(Object entity);

    <T> T getRemoved(Class<T> entityClass, long id);

    boolean isPersisted(Object entity);

    default Type getType(long id) {
        return getEntity(Type.class, id);
    }

    default Type getType(RefDTO ref) {
        return getEntity(Type.class, ref);
    }

    default ClassType getClassType(long id) {
        return getEntity(ClassType.class, id);
    }

    default ClassType getClassType(RefDTO ref) {
        return getEntity(ClassType.class, ref);
    }

    UnionType getUnionType(Set<Type> members);

    @Nullable EventQueue getEventQueue();

    IntersectionType getIntersectionType(Set<Type> types);

    IntersectionTypeContext getIntersectionTypeContext();

    ArrayType getArrayType(Type elementType, ArrayKind kind);

    default ClassType getListType(Type elementType) {
        return getGenericContext().getParameterizedType(StandardTypes.getListType(), List.of(elementType));
    }

    default ClassType getReadWriteListType(Type elementType) {
        return getGenericContext().getParameterizedType(StandardTypes.getReadWriteListType(), List.of(elementType));
    }

    default ClassType getClassType(Long id, String code) {
        NncUtils.requireTrue(id != null || code != null, "id and code can't both be null");
        if(id != null) {
            return getClassType(id);
        }
        else {
            return selectFirstByKey(ClassType.UNIQUE_CODE, code);
        }
    }

    long getAppId(Object model);

    long getAppId();

    default Field getField(long id) {
        return getEntity(Field.class, id);
    }

    default Field getField(RefDTO ref) {
        return getEntity(Field.class, ref);
    }

    default TypeVariable getTypeVariable(RefDTO ref) {
        return getEntity(TypeVariable.class, ref);
    }

    default NodeRT getNode(long id) {
        return getEntity(NodeRT.class, id);
    }

    default NodeRT getNode(RefDTO ref) {
        return getEntity(NodeRT.class, ref);
    }

    default ScopeRT getScope(long id) {
        return getEntity(ScopeRT.class, id);
    }

    default Flow getFlow(long id) {
        return getEntity(Flow.class, id);
    }

    default Flow getFlow(RefDTO ref) {
        return getEntity(Flow.class, ref);
    }

    default Method getMethod(RefDTO ref) {
        return getEntity(Method.class, ref);
    }

    default Method getMethod(long id) {
        return getEntity(Method.class, id);
    }

    default ObjectMapping getObjectMapping(RefDTO ref) {
        return getEntity(ObjectMapping.class, ref);
    }

    default Mapping getMapping(RefDTO ref) {
        return getEntity(Mapping.class, ref);
    }

    default Function getFunction(RefDTO ref) {
        return getEntity(Function.class, ref);
    }

    default Function getFunction(Long id) {
        return getEntity(Function.class, id);
    }

    default Mapping getMapping(long id) {
        return getEntity(Mapping.class, id);
    }

    default FieldsObjectMapping getObjectMapping(Long id) {
        return getEntity(FieldsObjectMapping.class, id);
    }

    boolean isFinished();

    void finish();

    IInstanceContext getInstanceContext();

    <T> List<T> query(EntityIndexQuery<T> query);

    long count(EntityIndexQuery<?> query);

    void updateInstances();

    void update(Object object);

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object...refValues);

    boolean remove(Object model);

    void batchRemove(List<?> entities);

    @Nullable
    default <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object...values) {
        return NncUtils.first(selectByKey(indexDef, values));
    }

    void initIds();

    default boolean tryBind(Object entity) {
        if(isBindSupported() && isNewEntity(entity)) {
            bind(entity);
            return true;
        }
        else
            return false;
    }

    <T> T bind(T entity);

    default boolean isBindSupported() {
        return true;
    }

    void initIdManually(Object model, long id);

    CompositeTypeContext<?> getCompositeTypeContext(TypeCategory category);

    Collection<CompositeTypeContext<?>> getCompositeTypeContexts();

    boolean isRemoved(Object entity);

    IEntityContext createSame(long appId);

}
