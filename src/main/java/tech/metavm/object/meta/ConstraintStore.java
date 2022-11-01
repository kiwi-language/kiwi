package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.entity.LoadingOption;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.persistence.mappers.ConstraintMapper;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class ConstraintStore implements EntityStore<ConstraintRT> {

    @Autowired
    private ConstraintMapper constraintMapper;

    @Override
    public List<ConstraintRT> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options) {
        List<ConstraintPO> constraintPOs = constraintMapper.selectByIds(ids);
        Set<Long> typeIds = NncUtils.mapUnique(constraintPOs, ConstraintPO::getTypeId);
        Map<Long, Type> typeMap = NncUtils.toEntityMap(context.batchGet(Type.class, typeIds));

        List<ConstraintRT> results = new ArrayList<>();
        for (ConstraintPO constraintPO : constraintPOs) {
            Type type = typeMap.get(constraintPO.getTypeId());
            NncUtils.getAndInvoke(type, t -> t.getConstraint(constraintPO.getId()), results::add);
        }
        return results;
    }

    @Override
    public void batchInsert(List<ConstraintRT> entities) {
        constraintMapper.batchInsert(NncUtils.map(entities, ConstraintRT::toPO));
    }

    @Override
    public int batchUpdate(List<ConstraintRT> entities) {
        return constraintMapper.batchUpdate(NncUtils.map(entities, ConstraintRT::toPO));
    }

    @Override
    public void batchDelete(List<ConstraintRT> entities) {
        constraintMapper.batchDelete(NncUtils.map(entities, Entity::getId));
    }

    @Override
    public Class<ConstraintRT> getEntityType() {
        return ConstraintRT.class;
    }

}
