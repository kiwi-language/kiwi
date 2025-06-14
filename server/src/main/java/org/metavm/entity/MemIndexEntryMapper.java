package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.IndexQueryPO;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.util.ContextUtil;
import org.metavm.util.DebugEnv;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class MemIndexEntryMapper implements IndexEntryMapper {

    public static final Logger logger = LoggerFactory.getLogger(MemIndexEntryMapper.class);

    private final Map<GlobalKey, List<IndexEntryPO>> key2items = new HashMap<>();
    private final NavigableSet<IndexEntryPO> entries = new TreeSet<>();
    private final Map<Id, List<IndexEntryPO>> instanceId2items = new HashMap<>();

    private List<IndexEntryPO> getItems(GlobalKey key) {
        return key2items.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private List<IndexEntryPO> getItemsByInstanceId(Id instanceId) {
        return instanceId2items.computeIfAbsent(instanceId, k -> new ArrayList<>());
    }

    @Override
    public List<IndexEntryPO> query(IndexQueryPO query) {
        return query(query.appId(), query.indexId(), query.from(), query.to()).stream().toList();
    }

    @Override
    public long countRange(long appId, IndexKeyPO from, IndexKeyPO to) {
        if(!Arrays.equals(from.getIndexId(), to.getIndexId()))
            throw new InternalException("from.getIndexId() not equal to to.getIndexId()");
        return query(appId, from.getIndexId(), from, to).stream().map(IndexEntryPO::getId).distinct().count();
    }

    @Override
    public List<IndexEntryPO> scan(long appId, IndexKeyPO from, IndexKeyPO to) {
        if(!Arrays.equals(from.getIndexId(), to.getIndexId()))
            throw new InternalException("from.getIndexId() not equal to to.getIndexId()");
        return query(appId, from.getIndexId(), from, to).stream().toList();
    }

    public long count(IndexQueryPO query) {
        return query(query.appId(), query.indexId(), query.from(), query.to()).stream()
                .map(IndexEntryPO::getId)
                .distinct()
                .count();
    }

    public static final byte[] MIN_BYTES = new byte[0];

    public static final byte[] MAX_BYTES = new byte[] {
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final byte[] MIN_COLUMNS = new byte[0];
    private static final byte[] MAX_COLUMNS = new byte[] {0x01};

    public Collection<IndexEntryPO> query(long appId, byte[] indexId, @Nullable IndexKeyPO from, @Nullable IndexKeyPO to) {
        if(from == null)
            from = new IndexKeyPO(indexId, MIN_COLUMNS);
        if(to == null)
            to = new IndexKeyPO(indexId, MAX_COLUMNS);
        return entries.subSet(new IndexEntryPO(appId, from, MIN_BYTES), true, new IndexEntryPO(appId, to, MAX_BYTES), true);
    }

    @Override
    public List<IndexEntryPO> selectByInstanceIds(long appId, Collection<byte[]> instanceIds) {
        try (var ignored = ContextUtil.getProfiler().enter("MemIndexEntryMapper.selectByInstanceIds")) {
            return Utils.flatMap(instanceIds, id -> getItemsByInstanceId(Id.fromBytes(id)));
        }
    }

    @Override
    public List<IndexEntryPO> selectByKeys(long appId, Collection<IndexKeyPO> keys) {
        try (var ignored = ContextUtil.getProfiler().enter("MemIndexEntryMapper.selectByKeys")) {
            var globalKeys = Utils.map(keys, k -> new GlobalKey(appId, k));
            return Utils.flatMap(globalKeys, this::getItems);
        }
    }

    @Override
    public void batchInsert(Collection<IndexEntryPO> entries) {
        for (IndexEntryPO entry : entries) {
            if(!this.entries.add(entry))
                throw new InternalException("Duplicate index entry: " + entry);
            getItems(new GlobalKey(entry.getAppId(), entry.getKey())).add(entry);
            getItemsByInstanceId(entry.getId()).add(entry);
        }
    }

    @Override
    public void tryBatchInsert(Collection<IndexEntryPO> items) {
        for (IndexEntryPO entry : items) {
            if (Objects.equals(entry.getId(), Id.parse("0194a8d6b90704"))) {
                logger.debug("Inserting index entry with instance ID: {}, mapper: {}", entry.getId(),
                        System.identityHashCode(this));
                DebugEnv.object = entry;
            }
            if (this.entries.add(entry)) {
                getItems(new GlobalKey(entry.getAppId(), entry.getKey())).add(entry);
                getItemsByInstanceId(entry.getId()).add(entry);
            }
        }
    }

    @Override
    public void batchDelete(Collection<IndexEntryPO> items) {
        for (IndexEntryPO item : items) {
            if (Objects.equals(item.getId(), DebugEnv.id)) {
                logger.debug("Removing index entry with instance ID {}, mapper: {}",
                        item.getId(), System.identityHashCode(this));
                logger.debug("Contains to-delete: {}", this.entries.contains(item));
                logger.debug("Entries: {}", Utils.join(entries, IndexEntryPO::toString));
            }
            if(this.entries.remove(item)) {
                getItems(new GlobalKey(item.getAppId(), item.getKey())).remove(item);
                getItemsByInstanceId(item.getId()).remove(item);
            }
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
        copy.batchInsert(Utils.map(entries, IndexEntryPO::copy));
        return copy;
    }

}
