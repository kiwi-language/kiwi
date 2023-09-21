package tech.metavm.entity;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.Flow;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;

public interface IEntityContext extends ModelInstanceMap {

    boolean containsInstance(Instance instance);

    boolean containsModel(Object model);

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    <T> List<T> getByType(Class<T> type, T startExclusive, long limit);

    boolean existsInstances(Class<?> type);

    boolean containsEntity(Class<?> entityType, long id);

    <T> T getEntity(Class<T> entityType, long id);

    <T> T getEntity(Class<T> entityType, RefDTO reference);

    Type getType(Class<?> javaType);

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

    default ClassType getClassType(Long id, String code) {
        NncUtils.requireTrue(id != null || code != null, "id and code can't both be null");
        if(id != null) {
            return getClassType(id);
        }
        else {
            return (ClassType) selectByUniqueKey(Type.UNIQUE_CODE, code);
        }
    }

    default Field getField(long id) {
        return getEntity(Field.class, id);
    }

    default Field getField(RefDTO reference) {
        return getEntity(Field.class, reference);
    }

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

    void initIdManually(Object model, long id);

}
