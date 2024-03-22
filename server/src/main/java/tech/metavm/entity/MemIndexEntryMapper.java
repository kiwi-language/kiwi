package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.IndexQueryPO;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemIndexEntryMapper implements IndexEntryMapper {

    private final Map<GlobalKey, List<IndexEntryPO>> key2items = new HashMap<>();
    private final Set<IndexEntryPO> entries = new HashSet<>();
    private final Map<Id, List<IndexEntryPO>> instanceId2items = new HashMap<>();

    private List<IndexEntryPO> getItems(GlobalKey key) {
        return key2items.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private List<IndexEntryPO> getItemsByInstanceId(Id instanceId) {
        return instanceId2items.computeIfAbsent(instanceId, k -> new ArrayList<>());
    }

    @Override
    public List<IndexEntryPO> query(IndexQueryPO query) {
        return query.execute(new HashSet<>(entries));
    }

    @Override
    public long countRange(long appId, IndexKeyPO from, IndexKeyPO to) {
        if(!Arrays.equals(from.getIndexId(), to.getIndexId()))
            throw new InternalException("from.getIndexId() not equal to to.getIndexId()");
        long count = 0;
        for (IndexEntryPO entry : entries) {
            if (entry.getAppId() == appId && Arrays.equals(entry.getIndexId(), from.getIndexId()) &&
                    entry.getKey().compareTo(from) >= 0 && entry.getKey().compareTo(to) <= 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<IndexEntryPO> scan(long appId, IndexKeyPO from, IndexKeyPO to) {
        if(!Arrays.equals(from.getIndexId(), to.getIndexId()))
            throw new InternalException("from.getIndexId() not equal to to.getIndexId()");
        var result = new ArrayList<IndexEntryPO>();
        for (IndexEntryPO entry : entries) {
            if (entry.getAppId() == appId && Arrays.equals(entry.getIndexId(), from.getIndexId()) &&
                    entry.getKey().compareTo(from) >= 0 && entry.getKey().compareTo(to) <= 0) {
                result.add(entry.copy());
            }
        }
        return result;
    }

    public long count(IndexQueryPO query) {
        return query.count(entries);
    }

    @Override
    public List<IndexEntryPO> selectByInstanceIdsOrKeys(long appId,
                                                        Collection<byte[]> instanceIds,
                                                        Collection<IndexKeyPO> keys) {
        var globalKeys = NncUtils.map(keys, k -> new GlobalKey(appId, k));
        return NncUtils.union(
                NncUtils.flatMap(instanceIds, id -> getItemsByInstanceId(Id.fromBytes(id))),
                NncUtils.flatMap(globalKeys, this::getItems)
        );
    }

    @Override
    public List<IndexEntryPO> selectByInstanceIds(long appId, Collection<byte[]> instanceIds) {
        return NncUtils.flatMap(instanceIds, id -> getItemsByInstanceId(Id.fromBytes(id)));
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
            getItemsByInstanceId(item.getId()).add(item);
            this.entries.add(item);
        }
    }

    @Override
    public void batchDelete(Collection<IndexEntryPO> items) {
        for (IndexEntryPO item : items) {
            if(!this.entries.remove(item))
                throw new InternalException(item + " does not exist");
            getItems(new GlobalKey(item.getAppId(), item.getKey())).remove(item);
            getItemsByInstanceId(item.getId()).remove(item);
        }
    }

    @SuppressWarnings("unused")
    public void clear() {
        entries.clear();
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
        copy.batchInsert(NncUtils.map(entries, IndexEntryPO::copy));
        return copy;
    }

}
