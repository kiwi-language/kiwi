package org.metavm.object.instance.persistence.mappers;

import org.metavm.object.instance.ScanQuery;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MemInstanceMapper implements InstanceMapper {

    public static final Logger logger = LoggerFactory.getLogger(MemInstanceMapper.class);

    private final NavigableMap<Long, InstancePO> id2instance = new TreeMap<>();
    private final Map<Long, InstancePO> removed = new HashMap<>();
    private String table;


    public MemInstanceMapper(String table) {
        this.table = table;
    }

    @Override
    public InstancePO selectById(long id) {
        return id2instance.get(id);
    }

    @Override
    public List<InstancePO> selectByIds(long appId, Collection<Long> ids) {
        return Utils.mapAndFilter(ids, id2instance::get, i -> i != null && i.getAppId() == appId);
    }

    @Override
    public void batchUpdate(Collection<InstancePO> records) {
        for (InstancePO record : records) {
            var instance = Objects.requireNonNull(id2instance.get(record.getId()),
                    () -> "Instance " + record.getId() + " not found in table " + table);
            instance.setVersion(record.getVersion());
            instance.setSyncVersion(record.getSyncVersion());
            instance.setNextNodeId(record.getNextNodeId());
            if(record.getData() != null)
                instance.setData(record.getData());
        }
    }

    @Override
    public void batchUpsert(Collection<InstancePO> records) {
        var inserts = Utils.exclude(records, i -> id2instance.containsKey(i.getId()));
        var updates = Utils.filter(records, i -> id2instance.containsKey(i.getId()));
        batchInsert(inserts);
        batchUpdate(updates);
    }

    @Override
    public void batchInsert(Collection<InstancePO> records) {
        records.forEach(this::add);
    }

    @Override
    public void batchDelete(long appId,
                            long timestamp,
                            Collection<VersionPO> versions) {
        for (VersionPO version : versions) {
            InstancePO instancePO = Objects.requireNonNull(id2instance.get(version.id()));
            remove(version.id());
            removed.put(version.id(), instancePO);
        }
    }

    @Override
    public void tryBatchDelete(long appId, long timestamp, Collection<VersionPO> versions) {
        for (VersionPO version : versions) {
            var instancePO = id2instance.get(version.id());
            if (instancePO != null) {
                remove(version.id());
                removed.put(version.id(), instancePO);
            }
        }
    }

    private void add(InstancePO instancePO) {
        instancePO = instancePO.copy();
        Utils.require(id2instance.get(instancePO.getId()) == null,
                "Instance with id " + instancePO.getId() + " already exists");
        id2instance.put(instancePO.getId(), instancePO);
//        type2instances.computeIfAbsent(Id.fromBytes(instancePO.getTypeId()), k -> new ArrayList<>()).add(instancePO);
//        forest.computeIfAbsent(instancePO.getId(), k -> new HashSet<>()).add(instancePO);
    }

    private void remove(long id) {
        InstancePO instancePO = id2instance.remove(id);
        if(instancePO == null) {
            throw new InternalException("Instance " + id + " does not exist");
        }
    }

    public List<InstancePO> head() {
        List<InstancePO> values = new ArrayList<>(id2instance.values());
        return values.subList(0, Math.min(100, values.size()));
    }

    @Override
    public int updateSyncVersion(List<VersionPO> versions) {
        for (VersionPO version : versions) {
            InstancePO instancePO = id2instance.get(version.id());
            if(instancePO == null) {
                instancePO = Objects.requireNonNull(removed.get(version.id()));
            }
            instancePO.setSyncVersion(version.version());
        }
        return versions.size();
    }

    @Override
    public List<Long> scanTrees(long appId, long startId, long limit) {
        return id2instance.tailMap(startId, false).values().stream()
                .filter(i -> i.getAppId() == appId)
                .map(InstancePO::getId)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<TreeVersion> selectVersions(long appId, List<Long> ids) {
        return ids.stream()
//                .map(Id::fromBytes)
                .map(id2instance::get)
//                .filter(Objects::nonNull)
//                .map(InstancePO::getRootInstanceId)
//                .map(id2instance::get)
                .filter(Objects::nonNull)
                .distinct()
                .map(i -> new TreeVersion(i.getId(), i.getVersion()))
                .collect(Collectors.toList());
    }

    private List<InstancePO> scan(long appId, ScanQuery query) {
        var tail = id2instance.tailMap(query.startId(), false).values();
        var result = new ArrayList<InstancePO>();
        for (InstancePO instancePO : tail) {
            if(result.size() >= query.limit()) {
                break;
            }
            if(instancePO.getAppId() == appId) {
                result.add(instancePO);
            }
        }
        return result;
    }

    public MemInstanceMapper copy() {
        var copy = new MemInstanceMapper(table);
        id2instance.values().forEach(i -> copy.add(i.copy()));
        removed.values().forEach(i -> copy.removed.put(i.getId(), i.copy()));
        return copy;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Collection<Long> getTreeIds() {
        return Collections.unmodifiableCollection(id2instance.keySet());
    }

}
