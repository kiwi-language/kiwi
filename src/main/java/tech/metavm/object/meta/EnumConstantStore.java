package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.entity.LoadingOption;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class EnumConstantStore implements EntityStore<EnumConstant> {

    @Autowired
    private InstanceMapper instanceMapper;

    @Override
    public List<EnumConstant> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options) {
        List<InstancePO> instancePOs = instanceMapper.selectByIds(context.getTenantId(), ids);
        Set<Long> enumIds = NncUtils.mapUnique(instancePOs, InstancePO::typeId);
        List<Type> types = context.batchGet(Type.class, enumIds);
        Map<Long, Type> typeMap = NncUtils.toEntityMap(types);
        return NncUtils.mapAndFilter(
                instancePOs,
                instancePO -> NncUtils.get(typeMap.get(instancePO.typeId()), t -> t.getEnumConstant(instancePO.id())),
                Objects::nonNull
        );
    }

    @Override
    public void batchInsert(List<EnumConstant> entities) {
        instanceMapper.batchInsert(NncUtils.map(entities, EnumConstant::toPO));
    }

    @Override
    public int batchUpdate(List<EnumConstant> entities) {
        return instanceMapper.batchUpdate(NncUtils.map(entities, EnumConstant::toPO));
    }

    @Override
    public void batchDelete(List<EnumConstant> entities) {
        if(NncUtils.isNotEmpty(entities)) {
            instanceMapper.batchDelete(
                    entities.get(0).getTenantId(),
                    NncUtils.map(entities, EnumConstant::nextVersion)
            );
        }
    }

    @Override
    public Class<EnumConstant> getEntityType() {
        return EnumConstant.class;
    }
}
