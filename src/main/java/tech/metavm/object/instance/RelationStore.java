package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.ValueStore;
import tech.metavm.object.instance.persistence.mappers.RelationMapper;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class RelationStore implements ValueStore<InstanceRelation> {

    private final RelationMapper relationMapper;

    public RelationStore(RelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    @Override
    public void batchInsert(List<InstanceRelation> inserts) {
        relationMapper.batchInsert(NncUtils.map(inserts, InstanceRelation::toPO));
    }

    @Override
    public void batchDelete(List<InstanceRelation> deletes) {
        relationMapper.batchDelete(NncUtils.map(deletes, InstanceRelation::toPO));
    }

    @Override
    public Class<InstanceRelation> getType() {
        return InstanceRelation.class;
    }
}
