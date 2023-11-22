package tech.metavm.entity;

import tech.metavm.common.RefDTO;
import tech.metavm.flow.Flow;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.*;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.NncUtils;
import tech.metavm.util.profile.Profiler;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IEntityContext extends ModelInstanceMap, Closeable {

    boolean containsModel(Object model);

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, RefDTO ref) {
        return getEntity(typeReference.getType(), ref);
    }

    void invalidateCache(long id);

    Profiler getProfiler();

    DefContext getDefContext();

    <T> List<T> getByType(Class<? extends T> type, T startExclusive, long limit);

    default List<ClassType> getTemplateInstances(ClassType template) {
        NncUtils.requireTrue(template.isTemplate());
        return selectByKey(ClassType.TEMPLATE_IDX,template);
    }

    <T> List<T> getAllCachedEntities(Class<T> entityClass);

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

    <T> T getEntity(Class<T> entityType, RefDTO reference);

    Type getType(Class<?> javaType);

    IEntityContext getParent();

    <T> T createEntity(Instance instance, ModelDef<T, ?> def);

    void setLoadWithCache(Object entity);

    default FunctionType getFunctionType(RefDTO ref) {
        return getEntity(FunctionType.class, ref);
    }

    boolean isNewEntity(Object entity);

    <T> T getRemoved(Class<T> entityClass, long id);

    boolean isPersisted(Object entity);

    default Type getType(long id) {
        return getEntity(Type.class, id);
    }

    default Type getType(RefDTO reference) {
        return getEntity(Type.class, reference);
    }

    default ClassType getClassType(long id) {
        return getEntity(ClassType.class, id);
    }

    default ClassType getClassType(RefDTO ref) {
        return getEntity(ClassType.class, ref);
    }

    UnionType getUnionType(Set<Type> members);

    IntersectionType getIntersectionType(Set<Type> types);

    IntersectionTypeContext getIntersectionTypeContext();

    ArrayType getArrayType(Type elementType, ArrayKind kind);

    default ClassType getClassType(Long id, String code) {
        NncUtils.requireTrue(id != null || code != null, "id and code can't both be null");
        if(id != null) {
            return getClassType(id);
        }
        else {
            return selectByUniqueKey(ClassType.UNIQUE_CODE, code);
        }
    }

    long getTenantId(Object model);

    long getTenantId();

    default Field getField(long id) {
        return getEntity(Field.class, id);
    }

    default Field getField(RefDTO reference) {
        return getEntity(Field.class, reference);
    }

    default TypeVariable getTypeVariable(RefDTO reference) {
        return getEntity(TypeVariable.class, reference);
    }

    void afterContextIntIds();

    default RoleRT getRole(long id) {
        return getEntity(RoleRT.class, id);
    }

    default UserRT getUser(long id) {
        return getEntity(UserRT.class, id);
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

    boolean isFinished();

    void finish();

    IInstanceContext getInstanceContext();

    <T> List<T> query(EntityIndexQuery<T> query);

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object...refValues);

    boolean remove(Object model);

    void batchRemove(List<?> entities);

    @Nullable
    default <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...values) {
        return NncUtils.getFirst(selectByKey(indexDef, values));
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

    void bind(Object entity);

    boolean isBindSupported();

    void initIdManually(Object model, long id);

    CompositeTypeContext<?> getCompositeTypeContext(TypeCategory category);

    Collection<CompositeTypeContext<?>> getCompositeTypeContexts();

    boolean isRemoved(Object entity);
}
