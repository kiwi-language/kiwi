package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.Version;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.DiffUtils;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MemInstanceMapper implements InstanceMapper {

    private final NavigableMap<Id, InstancePO> id2instance = new TreeMap<>();
    private final Map<Id, List<InstancePO>> type2instances = new HashMap<>();
    private final Map<Id, InstancePO> removed = new HashMap<>();
    private final Map<Id, Set<InstancePO>> forest = new HashMap<>();

    @Override
    public List<InstancePO> selectByTypeIds(long appId, Collection<ByTypeQuery> queries) {
        List<InstancePO> result = NncUtils.flatMap(
                queries,
                q -> queryByType(appId, q)
        );
        return NncUtils.deduplicateAndSort(result, Comparator.comparing(InstancePO::getInstanceId));
    }

    @Override
    public List<InstancePO> selectForest(long appId, Collection<byte[]> ids, int lockMode) {
        List<Version> rootVersions = selectRootVersions(appId, new ArrayList<>(ids));
        var rootIds = NncUtils.map(rootVersions, Version::getInstanceId);
        return NncUtils.flatMap(rootIds, id -> forest.getOrDefault(id, Set.of()));
    }

    private List<InstancePO> queryByType(long appId, ByTypeQuery query) {
        List<InstancePO> result = NncUtils.filter(
                type2instances.get(query.getTypeId()),
                instancePO -> instancePO.getAppId() == appId
        );
        return result.subList(0, Math.min((int) query.getLimit(), result.size()));
    }

    @Override
    public InstancePO selectById(byte[] id) {
        return id2instance.get(Id.fromBytes(id));
    }

    @Override
    public List<InstancePO> selectByIds(long appId, Collection<byte[]> ids, int lockMode) {
        return NncUtils.mapAndFilter(
                ids,
                id -> id2instance.get(Id.fromBytes(id)),
                i -> i != null && i.getAppId() == appId
        );
    }

    @Override
    public void batchUpdate(Collection<InstancePO> records) {
        for (InstancePO record : records) {
            var instance = Objects.requireNonNull(id2instance.get(record.getInstanceId()));
            instance.setVersion(record.getVersion());
            instance.setSyncVersion(record.getSyncVersion());
            instance.setParentFieldId(record.getParentFieldId());
            instance.setParentId(record.getParentId());
            instance.setTitle(record.getTitle());
            instance.setRootId(record.getRootId());
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
            InstancePO instancePO = NncUtils.requireNonNull(id2instance.get(version.getInstanceId()));
            remove(Id.fromBytes(version.id()));
            removed.put(version.getInstanceId(), instancePO);
        }
    }

    private void add(InstancePO instancePO) {
        instancePO = DiffUtils.copyPojo(instancePO);
        NncUtils.requireNull(id2instance.get(instancePO.getInstanceId()),
                "Instance with id " + instancePO.getInstanceId() + " already exists");
        id2instance.put(instancePO.getInstanceId(), instancePO);
        type2instances.computeIfAbsent(Id.fromBytes(instancePO.getTypeId()), k -> new ArrayList<>()).add(instancePO);
        forest.computeIfAbsent(instancePO.getRootInstanceId(), k -> new HashSet<>()).add(instancePO);
    }

    private void remove(Id id) {
        InstancePO instancePO = id2instance.remove(id);
        if(instancePO == null) {
            throw new InternalException("Instance " + id + " does not exist");
        }
        var tree = forest.get(instancePO.getRootInstanceId());
        if(tree != null)
            tree.remove(instancePO);
        type2instances.get(Id.fromBytes(instancePO.getTypeId())).removeIf(i -> Arrays.equals(i.getId(), instancePO.getId()));
    }

    public List<InstancePO> head() {
        List<InstancePO> values = new ArrayList<>(id2instance.values());
        return values.subList(0, Math.min(100, values.size()));
    }

    @Override
    public List<byte[]> getAliveIds(long appId, Collection<byte[]> ids) {
        return NncUtils.filter(ids, id -> {
            InstancePO instanceArrayPO = id2instance.get(Id.fromBytes(id));
            return instanceArrayPO != null && instanceArrayPO.getAppId() == appId;
        });
    }

    @Override
    public int updateSyncVersion(List<VersionPO> versions) {
        for (VersionPO version : versions) {
            InstancePO instancePO = id2instance.get(version.getInstanceId());
            if(instancePO == null) {
                instancePO = NncUtils.requireNonNull(removed.get(version.getInstanceId()));
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
        result.sort(Comparator.comparing(InstancePO::getInstanceId));
        return result;
    }

    @Override
    public List<Long> selectVersions(List<byte[]> ids) {
        return NncUtils.map(ids, id -> id2instance.get(Id.fromBytes(id)).getVersion());
    }

    @Override
    public List<Version> selectRootVersions(long appId, List<byte[]> ids) {
        return ids.stream()
                .map(Id::fromBytes)
                .map(id2instance::get)
                .filter(Objects::nonNull)
                .map(InstancePO::getRootInstanceId)
                .map(id2instance::get)
                .filter(Objects::nonNull)
                .distinct()
                .map(i -> new Version(i.getId(), i.getVersion()))
                .collect(Collectors.toList());
    }

    private List<InstancePO> scan(long appId, ScanQuery query) {
        var tail = id2instance.tailMap(Id.fromBytes(query.startId()), false).values();
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
        removed.values().forEach(i -> copy.removed.put(i.getInstanceId(), i.copy()));
        return copy;
    }

}
