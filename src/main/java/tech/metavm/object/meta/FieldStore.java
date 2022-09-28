package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.mappers.FieldMapper;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class FieldStore implements EntityStore<Field> {

    @Autowired
    private FieldMapper fieldMapper;

    @Override
    public List<Field> batchGet(Collection<Long> ids, EntityContext context) {
        List<FieldPO> fieldPOs = fieldMapper.selectByIds(ids);
        Set<Long> ownerIds = NncUtils.mapUnique(fieldPOs, FieldPO::getOwnerId);
        List<Type> types = context.batchGet(Type.class, ownerIds);
        Map<Long, Type> typeMap = NncUtils.toMap(types, Type::getId);
        List<Field> results = new ArrayList<>();
        for (FieldPO fieldPO : fieldPOs) {
            Type owner = typeMap.get(fieldPO.getOwnerId());
            Field field = NncUtils.get(owner, t -> t.getField(fieldPO.getId()));
            if(field != null) {
                results.add(field);
            }
        }
        return results;
    }

    @Override
    public void batchInsert(List<Field> fields) {
        fieldMapper.batchInsert(NncUtils.map(fields, Field::toPO));
    }

    @Override
    public int batchUpdate(List<Field> fields) {
        return fieldMapper.batchUpdate(NncUtils.map(fields, Field::toPO));
    }

    @Override
    public void batchDelete(List<Long> ids) {
        fieldMapper.batchDelete(ids);
    }

    @Override
    public Class<Field> getEntityType() {
        return Field.class;
    }
}
