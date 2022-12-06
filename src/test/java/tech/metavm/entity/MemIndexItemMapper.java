package tech.metavm.entity;

import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.mappers.IndexItemMapper;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemIndexItemMapper implements IndexItemMapper {

    private final Map<IndexKeyPO, List<IndexItemPO>> key2items = new HashMap<>();
    private final Map<Long, List<IndexItemPO>> instanceId2items = new HashMap<>();

    @Override
    public Long countByKeys(long tenantId, List<IndexKeyPO> keys) {
        long count = 0L;
        for (IndexKeyPO key : keys) {
            count += getItems(key).size();
        }
        return count;
    }

    private List<IndexItemPO> getItems(IndexKeyPO key) {
        return key2items.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private List<IndexItemPO> getItemsByInstanceId(long instanceId) {
        return instanceId2items.computeIfAbsent(instanceId, k -> new ArrayList<>());
    }

    @Override
    public List<IndexItemPO> selectByKeys(long tenantId, List<IndexKeyPO> keys) {
        return NncUtils.flatMap(
                keys,
                this::getItems
        );
    }

    @Override
    public List<IndexItemPO> selectByInstanceIdsOrKeys(long tenantId, List<Long> instanceIds, List<IndexKeyPO> keys) {
        return NncUtils.merge(
                NncUtils.flatMap(instanceIds, this::getItemsByInstanceId),
                NncUtils.flatMap(keys, this::getItems)
        );
    }

    @Override
    public void batchInsert(Collection<IndexItemPO> items) {
        for (IndexItemPO item : items) {
            getItems(item.getKey()).add(item);
            getItemsByInstanceId(item.getInstanceId()).add(item);
        }
    }

    @Override
    public void batchDelete(Collection<IndexItemPO> items) {
        for (IndexItemPO item : items) {
            getItems(item.getKey()).remove(item);
            getItemsByInstanceId(item.getInstanceId()).remove(item);
        }
    }

    public void clear() {
        key2items.clear();
        instanceId2items.clear();
    }

}
