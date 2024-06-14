package org.metavm.object.instance.persistence.mappers;

import org.metavm.object.instance.ScanQuery;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.util.DiffUtils;
import org.metavm.util.IdentitySet;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MemInstanceMapper implements InstanceMapper {

    private final NavigableMap<Long, InstancePO> id2instance = new TreeMap<>();
//    private final Map<Id, List<InstancePO>> type2instances = new HashMap<>();
    private final Map<Long, InstancePO> removed = new HashMap<>();
    private final Map<Long, Set<InstancePO>> forest = new HashMap<>();

//    @Override
//    public List<InstancePO> selectByTypeIds(long appId, Collection<ByTypeQuery> queries) {
//        List<InstancePO> result = NncUtils.flatMap(
//                queries,
//                q -> queryByType(appId, q)
//        );
//        return NncUtils.deduplicateAndSort(result, Comparator.comparing(InstancePO::getId));
//    }

    //    private List<InstancePO> queryByType(long appId, ByTypeQuery query) {
//        List<InstancePO> result = NncUtils.filter(
//                type2instances.get(query.getTypeId()),
//                instancePO -> instancePO.getAppId() == appId
//        );
//        return result.subList(0, Math.min((int) query.getLimit(), result.size()));
//    }

    @Override
    public InstancePO selectById(long id) {
        return id2instance.get(id);
    }

    @Override
    public List<InstancePO> selectByIds(long appId, Collection<Long> ids, int lockMode) {
        return NncUtils.mapAndFilter(ids, id2instance::get, i -> i != null && i.getAppId() == appId);
    }

    @Override
    public void batchUpdate(Collection<InstancePO> records) {
        for (InstancePO record : records) {
            var instance = Objects.requireNonNull(id2instance.get(record.getId()));
            instance.setVersion(record.getVersion());
            instance.setSyncVersion(record.getSyncVersion());
            instance.setNextNodeId(record.getNextNodeId());
            if(record.getData() != null)
                instance.setData(record.getData());
        }
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
            InstancePO instancePO = NncUtils.requireNonNull(id2instance.get(version.id()));
            remove(version.id());
            removed.put(version.id(), instancePO);
        }
    }

    private void add(InstancePO instancePO) {
        instancePO = DiffUtils.copyPojo(instancePO);
        NncUtils.requireNull(id2instance.get(instancePO.getId()),
                "Instance with id " + instancePO.getId() + " already exists");
        id2instance.put(instancePO.getId(), instancePO);
//        type2instances.computeIfAbsent(Id.fromBytes(instancePO.getTypeId()), k -> new ArrayList<>()).add(instancePO);
        forest.computeIfAbsent(instancePO.getId(), k -> new HashSet<>()).add(instancePO);
    }

    private void remove(long id) {
        InstancePO instancePO = id2instance.remove(id);
        if(instancePO == null) {
            throw new InternalException("Instance " + id + " does not exist");
        }
        var tree = forest.get(instancePO.getId());
        if(tree != null)
            tree.remove(instancePO);
//        type2instances.get(Id.fromBytes(instancePO.getTypeId())).removeIf(i -> Arrays.equals(i.getId(), instancePO.getId()));
    }

    public List<InstancePO> head() {
        List<InstancePO> values = new ArrayList<>(id2instance.values());
        return values.subList(0, Math.min(100, values.size()));
    }

//    @Override
//    public List<byte[]> getAliveIds(long appId, Collection<byte[]> ids) {
//        return NncUtils.filter(ids, id -> {
//            InstancePO instanceArrayPO = id2instance.get(Id.fromBytes(id));
//            return instanceArrayPO != null && instanceArrayPO.getAppId() == appId;
//        });
//    }

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
    public List<InstancePO> scan(long appId, Collection<ScanQuery> queries) {
        Set<InstancePO> uniqueResult = new IdentitySet<>();
        for (ScanQuery query : queries) {
            uniqueResult.addAll(scan(appId, query));
        }
        List<InstancePO> result = new ArrayList<>(uniqueResult);
        result.sort(Comparator.comparing(InstancePO::getId));
        return result;
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
        var copy = new MemInstanceMapper();
        id2instance.values().forEach(i -> copy.add(i.copy()));
        removed.values().forEach(i -> copy.removed.put(i.getId(), i.copy()));
        return copy;
    }

}
