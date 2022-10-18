package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.entity.LoadingOption;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.mappers.FieldMapper;
import tech.metavm.object.meta.persistence.mappers.TypeMapper;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class FieldStore implements EntityStore<Field> {

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private TypeMapper typeMapper;

    @Override
    public List<Field> batchGet(Collection<Long> ids, EntityContext context, EnumSet<LoadingOption> options) {
        List<FieldPO> fieldPOs = fieldMapper.selectByIds(ids);
        Set<Long> ownerIds = NncUtils.mapUnique(fieldPOs, FieldPO::getOwnerId);
        List<Type> types = context.batchGet(Type.class, ownerIds, options);
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

    public List<String> getReferringFieldNames(Type type) {
        List<FieldPO> fieldPOs = fieldMapper.selectByTypeIds(List.of(type.getId()));
        if(NncUtils.isEmpty(fieldPOs)) {
            return List.of();
        }
        Set<Long> ownerIds = NncUtils.mapUnique(fieldPOs, FieldPO::getOwnerId);
        List<TypePO> typePOs = typeMapper.selectByIds(ownerIds);
        Map<Long, TypePO> typePOMap = NncUtils.toMap(typePOs, TypePO::getId);
        List<String> results = new ArrayList<>();
        for (FieldPO fieldPO : fieldPOs) {
            TypePO owner = typePOMap.get(fieldPO.getOwnerId());
            if(owner != null && !owner.getId().equals(type.getId())) {
                results.add(owner.getName() + "." + fieldPO.getName());
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
