package tech.metavm.entity;

import tech.metavm.flow.FlowRT;
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

import java.util.List;

public interface IEntityContext extends ModelInstanceMap {

    boolean containsInstance(Instance instance);

    boolean containsModel(Object model);

    default <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    boolean containsEntity(Class<? extends Entity> entityType, long id);

    <T extends Entity> T getEntity(Class<T> entityType, long id);

    default Type getType(long id) {
        return getEntity(Type.class, id);
    }

    default ClassType getClassType(long id) {
        return getEntity(ClassType.class, id);
    }

    default Field getField(long id) {
        return getEntity(Field.class, id);
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

    default ScopeRT getScope(long id) {
        return getEntity(ScopeRT.class, id);
    }

    default FlowRT getFlow(long id) {
        return getEntity(FlowRT.class, id);
    }

    boolean isFinished();

    void finish();

    IInstanceContext getInstanceContext();

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object...refValues);

    boolean remove(Entity entity);

    default <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...refValues) {
        return NncUtils.getFirst(selectByKey(indexDef, refValues));
    }

    void initIds();

    void bind(Object entity);

}
