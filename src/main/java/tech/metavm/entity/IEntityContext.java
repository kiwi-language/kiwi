package tech.metavm.entity;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.Flow;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.ArrayKind;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.generic.*;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.List;
import java.util.Set;

public interface IEntityContext extends ModelInstanceMap, Closeable {

    boolean containsInstance(Instance instance);

    boolean containsModel(Object model);

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, RefDTO ref) {
        return getEntity(typeReference.getType(), ref);
    }

    DefContext getDefContext();

    <T> List<T> getByType(Class<T> type, T startExclusive, long limit);

    default List<ClassType> getTemplateInstances(ClassType template) {
        NncUtils.requireTrue(template.isTemplate());
        return selectByKey(ClassType.TEMPLATE_IDX,template);
    }

    void close();

    boolean existsInstances(Class<?> type);

    boolean containsEntity(Class<?> entityType, long id);

    ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments);

    FunctionType getFunctionType(List<Type> parameterTypes, Type returnType);

    GenericContext getGenericContext();

    FunctionTypeContext getFunctionTypeContext();

    UncertainType getUncertainType(Type lowerBound, Type upperBound);

    UnionType getNullableType(Type type);

    UncertainTypeContext getUncertainTypeContext();

    ArrayTypeContext getArrayTypeContext(ArrayKind kind);

    UnionTypeContext getUnionTypeContext();

    Set<CompositeType> getNewCompositeTypes();

    <T> T getEntity(Class<T> entityType, long id);

    <T> T getEntity(Class<T> entityType, RefDTO reference);

    Type getType(Class<?> javaType);

    default FunctionType getFunctionType(RefDTO ref) {
        return getEntity(FunctionType.class, ref);
    }

    boolean isNewEntity(Object entity);

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

    @Nullable IInstanceContext getInstanceContext();

    <T> List<T> query(EntityIndexQuery<T> query);

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object...refValues);

    boolean remove(Object model);

    void batchRemove(List<?> entities);

    @Nullable
    default <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...values) {
        return NncUtils.getFirst(selectByKey(indexDef, values));
    }

    void initIds();

    void bind(Object entity);

    boolean isBindSupported();

    void initIdManually(Object model, long id);

}
