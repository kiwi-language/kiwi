package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.ContextAttributeKey;
import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.LoadingOption;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.persistence.mappers.IndexItemMapper;
import tech.metavm.object.instance.persistence.mappers.InstanceMapperGateway;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InstanceStore implements IInstanceStore {

    private static final long BY_TYPE_LIMIT = 50;

    private final InstanceMapperGateway instanceMapperGateway;
    private final IndexItemMapper indexItemMapper;

    public InstanceStore(InstanceMapperGateway instanceMapperGateway, IndexItemMapper indexItemMapper) {
        this.instanceMapperGateway = instanceMapperGateway;
        this.indexItemMapper = indexItemMapper;
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        if(NncUtils.isNotEmpty(diff.inserts())) {
            instanceMapperGateway.batchInsert(diff.inserts());
        }
        if(NncUtils.isNotEmpty(diff.updates())) {
            instanceMapperGateway.batchUpdate(diff.updates());
        }
        if(NncUtils.isNotEmpty(diff.deletes())) {
            instanceMapperGateway.batchDelete(diff.deletes());
        }
    }

    public IInstance selectByUniqueKey(IndexKeyPO key, InstanceContext context) {
        return NncUtils.getFirst(selectByKey(key, context));
    }

    @Override
    public List<Instance> selectByKey(IndexKeyPO key, InstanceContext context) {
        List<IndexItemPO> indexItems = indexItemMapper.selectByKeys(context.getTenantId(), List.of(key));
        return context.batchGet(NncUtils.map(indexItems, IndexItemPO::getInstanceId));
    }

    public boolean updateSyncVersion(List<VersionPO> versions) {
        return instanceMapperGateway.updateSyncVersion(versions) == versions.size();
    }

    @Override
    public List<InstancePO> load(StoreLoadRequest request, InstanceContext context) {
        if(NncUtils.isEmpty(request.ids())) {
            return List.of();
        }
        List<InstancePO> records = instanceMapperGateway.selectByIds(context.getTenantId(), request.ids());
        Set<Long> typeIds = NncUtils.mapUnique(records, InstancePO::getTypeId);
        context.batchGet(typeIds, LoadingOption.ENUM_CONSTANTS_LAZY_LOADING);
        return records;
    }

    public void loadTitles(List<Long> ids, InstanceContext context) {
        if(NncUtils.isEmpty(ids)) {
            return;
        }
        List<InstanceTitlePO> titlePOs =  instanceMapperGateway.selectTitleByIds(context.getTenantId(), ids);
        Map<Long, String> currentTitleMap = context.getAttribute(ContextAttributeKey.INSTANCE_TITLES);
        currentTitleMap.putAll(
                NncUtils.toMap(titlePOs, InstanceTitlePO::id, InstanceTitlePO::title)
        );
    }

    @Override
    public List<InstancePO> getByTypeIds(Collection<Long> typeIds, InstanceContext context)
    {
        return instanceMapperGateway.selectByInstanceTypeIds(context.getTenantId(), typeIds, 0,
                typeIds.size() * BY_TYPE_LIMIT);
    }

    public String getTitle(Long id, InstanceContext context) {
        Map<Long, String> titleMap = context.getAttribute(ContextAttributeKey.INSTANCE_TITLES);
        String title = titleMap.get(id);
        if(title != null) {
            return title;
        }
        return context.get(id).getTitle();
    }

}
