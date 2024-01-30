package tech.metavm.entity;

import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.IndexQueryPO;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemIndexEntryMapper implements IndexEntryMapper {

    private final Map<GlobalKey, List<IndexEntryPO>> key2items = new HashMap<>();
    private final Set<IndexEntryPO> items = new HashSet<>();
    private final Map<Long, List<IndexEntryPO>> instanceId2items = new HashMap<>();

    private List<IndexEntryPO> getItems(GlobalKey key) {
        return key2items.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private List<IndexEntryPO> getItemsByInstanceId(long instanceId) {
        return instanceId2items.computeIfAbsent(instanceId, k -> new ArrayList<>());
    }

    @Override
    public List<IndexEntryPO> query(IndexQueryPO query) {
        return query.execute(new HashSet<>(items));
    }

    public long count(IndexQueryPO query) {
        return query.count(items);
    }

    @Override
    public List<IndexEntryPO> selectByInstanceIdsOrKeys(long appId,
                                                        Collection<Long> instanceIds,
                                                        Collection<IndexKeyPO> keys) {
        var globalKeys = NncUtils.map(keys, k -> new GlobalKey(appId, k));
        return NncUtils.union(
                NncUtils.flatMap(instanceIds, this::getItemsByInstanceId),
                NncUtils.flatMap(globalKeys, this::getItems)
        );
    }

    @Override
    public List<IndexEntryPO> selectByInstanceIds(long appId, Collection<Long> instanceIds) {
        return NncUtils.flatMap(instanceIds, this::getItemsByInstanceId);
    }

    @Override
    public List<IndexEntryPO> selectByKeys(long appId, Collection<IndexKeyPO> keys) {
        var globalKeys = NncUtils.map(keys, k -> new GlobalKey(appId, k));
        return NncUtils.flatMap(globalKeys, this::getItems);
    }

    @Override
    public void batchInsert(Collection<IndexEntryPO> items) {
        for (IndexEntryPO item : items) {
            getItems(new GlobalKey(item.getAppId(), item.getKey())).add(item);
            getItemsByInstanceId(item.getInstanceId()).add(item);
            this.items.add(item);
        }
    }

    @Override
    public void batchDelete(Collection<IndexEntryPO> items) {
        for (IndexEntryPO item : items) {
            if(!this.items.remove(item))
                throw new InternalException(item + " does not exist");
            getItems(new GlobalKey(item.getAppId(), item.getKey())).remove(item);
            getItemsByInstanceId(item.getInstanceId()).remove(item);
        }
    }

    @SuppressWarnings("unused")
    public void clear() {
        items.clear();
        key2items.clear();
        instanceId2items.clear();
    }

    private record GlobalKey(
            long appId,
            IndexKeyPO key
    ) {

    }

    public MemIndexEntryMapper copy() {
        var copy = new MemIndexEntryMapper();
        copy.items.addAll(items);
        copy.key2items.putAll(key2items);
        copy.instanceId2items.putAll(instanceId2items);
        return copy;
    }

}
