package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.meta.StdTypeConstants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class InstanceEntityStore implements EntityStore<InstanceEntity> {

    @Autowired
    private InstanceQueryService instanceQueryService;

    public <T extends InstanceEntity> Page<T> query(Class<T> entityType,
                                                    int page,
                                                    int pageSize,
                                                    String searchText,
                                                    EntityContext context) {
        Page<Long> idPage = instanceQueryService.query(
                ContextUtil.getTenantId(),
                new InstanceQueryDTO(
                        StdTypeConstants.USER.ID,
                        searchText,
                        page,
                        pageSize
                )
        );
        return new Page<>(
                batchGet(entityType, idPage.data(), context, LoadingOption.none()),
                idPage.total()
        );
    }

    @Override
    public List<InstanceEntity> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options) {
        return batchGet(InstanceEntity.class, ids, context, options);
    }

    public <T extends InstanceEntity> List<T> batchGet(Class<T> entityType,
                                                       Collection<Long> ids,
                                                       EntityContext context,
                                                       Set<LoadingOption> options) {
        InstanceContext instanceContext = context.getInstanceContext();
        List<Instance> instances = instanceContext.batchGet(ids);
        return convertToEntities(instances, entityType);
    }

    public <T extends InstanceEntity> List<T> getByTypeIds(Class<T> entityType,
                                                       Collection<Long> typeIds,
                                                       EntityContext context) {
        InstanceContext instanceContext = context.getInstanceContext();
        List<Instance> instances = instanceContext.getByTypeIds(typeIds);
        return convertToEntities(instances, entityType);
    }

    public <T extends InstanceEntity> List<T> selectByKey(Class<T> entityType, IndexKeyPO indexKey, EntityContext context) {
        return convertToEntities(context.getInstanceContext().getByKey(indexKey), entityType);
    }

    private <T extends InstanceEntity> List<T> convertToEntities(List<Instance> instances, Class<T> entityType) {
        return NncUtils.map(
                instances,
                instance -> InstanceEntityFactory.create(instance, entityType)
        );
    }

    @Override
    public void batchInsert(List<InstanceEntity> entities) {
        NncUtils.forEach(entities, InstanceEntity::saveToContext);
    }

    @Override
    public int batchUpdate(List<InstanceEntity> entities) {
        NncUtils.forEach(entities, InstanceEntity::saveToContext);
        return entities.size();
    }

    @Override
    public void batchDelete(List<InstanceEntity> entities) {
        entities.forEach(InstanceEntity::removeFromContext);
    }

    public Class<InstanceEntity> getEntityType() {
        return InstanceEntity.class;
    }
}
