package tech.metavm.object.instance;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.type.ClassMember;
import tech.metavm.object.type.Type;
import tech.metavm.object.version.Versions;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
@Order(1)
public class MetaVersionPlugin implements ContextPlugin {

    @Override
    public boolean beforeSaving(EntityChange<InstancePO> change, IInstanceContext context) {
        if (!context.getEntityContext().isBindSupported() && context.getBindHook() == null)
            return false;
        var entityContext = context.getEntityContext();
        var changedEntities = new ArrayList<>();
        change.forEachInsertOrUpdate(i ->
                changedEntities.add(entityContext.getEntity(Object.class, i.getId()))
        );
        var changedTypes = new HashSet<Type>();
        for (Object entity : changedEntities) {
            if (entity instanceof Type type)
                changedTypes.add(type);
            else if (entity instanceof ClassMember classMember)
                changedTypes.add(classMember.getDeclaringType());
        }
        List<Object> removeEntities = NncUtils.mapAndFilter(change.deletes(),
                i -> entityContext.getRemoved(Object.class, i.getId()),
                Objects::nonNull
        );
        Set<Long> removedTypeIds = new HashSet<>();
        for (Object entity : removeEntities) {
            if (entity instanceof Type type)
                removedTypeIds.add(type.getIdRequired());
            else if (entity instanceof ClassMember classMember) {
                if (!entityContext.isRemoved(classMember.getDeclaringType()))
                    changedTypes.add(classMember.getDeclaringType());
            }
        }
        if (!changedTypes.isEmpty() || !removedTypeIds.isEmpty()) {
            Versions.create(changedTypes, removedTypeIds, context.getEntityContext());
            return true;
        } else
            return false;
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> change, IInstanceContext context) {

    }
}
