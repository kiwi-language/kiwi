package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.Version;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

public class MemInstanceMapper implements InstanceMapper {

    private final NavigableMap<Long, InstancePO> id2instance = new TreeMap<>();
    private final Map<Long, List<InstancePO>> type2instances = new HashMap<>();
    private final Map<Long, InstancePO> removed = new HashMap<>();
    private final Map<Long, ReadWriteLock> lockMap = new HashMap<>();
    private final Map<Long, Set<InstancePO>> forest = new HashMap<>();

    @Override
    public List<InstancePO> selectByTypeIds(long tenantId, Collection<ByTypeQuery> queries) {
        List<InstancePO> result = NncUtils.flatMap(
                queries,
                q -> queryByType(tenantId, q)
        );
        return NncUtils.deduplicateAndSort(result, Comparator.comparingLong(InstancePO::getId));
    }

    @Override
    public List<InstancePO> selectForest(long tenantId, Collection<Long> ids, int lockMode) {
        List<Version> rootVersions = selectRootVersions(tenantId, new ArrayList<>(ids));
        var rootIds = NncUtils.map(rootVersions, Version::getId);
        return NncUtils.flatMap(rootIds, id -> forest.getOrDefault(id, Set.of()));
    }

    private List<InstancePO> queryByType(long tenantId, ByTypeQuery query) {
        List<InstancePO> result = NncUtils.filter(
                type2instances.get(query.getTypeId()),
                instancePO -> instancePO.getTenantId() == tenantId
        );
        return result.subList(0, Math.min((int) query.getLimit(), result.size()));
    }

    @Override
    public InstancePO selectById(long id) {
        return id2instance.get(id);
    }

    @Override
    public List<InstancePO> selectByIds(long tenantId, Collection<Long> ids, int lockMode) {
        return NncUtils.mapAndFilter(
                ids,
                id2instance::get,
                i -> i != null && i.getTenantId() == tenantId
        );
    }

    @Override
    public void batchUpdate(Collection<InstancePO> records) {
        for (InstancePO record : records) {
            remove(record.getId());
            add(record);
        }
    }

    @Override
    public void batchInsert(Collection<InstancePO> records) {
        records.forEach(this::add);
    }

    @Override
    public void batchDelete(long tenantId,
                            long timestamp,
                            Collection<VersionPO> versions) {
        for (VersionPO version : versions) {
            InstancePO instancePO = NncUtils.requireNonNull(id2instance.get(version.id()));
            remove(version.id());
            removed.put(version.id(), instancePO);
        }
    }

    private void add(InstancePO instancePO) {
        instancePO = EntityUtils.copyPojo(instancePO);
        NncUtils.requireNull(id2instance.get(instancePO.getId()),
                "Instance with id " + instancePO.getId() + " already exists");
        id2instance.put(instancePO.getId(), instancePO);
        type2instances.computeIfAbsent(instancePO.getTypeId(), k -> new ArrayList<>()).add(instancePO);
        forest.computeIfAbsent(instancePO.getRootId(), k -> new HashSet<>()).add(instancePO);
    }

    private void remove(long id) {
        InstancePO instancePO = id2instance.remove(id);
        if(instancePO == null) {
            throw new InternalException("Instance " + id + " does not exist");
        }
        var tree = forest.get(instancePO.getRootId());
        if(tree != null)
            tree.remove(instancePO);
        type2instances.get(instancePO.getTypeId()).removeIf(i -> Objects.equals(i.getId(), instancePO.getId()));
    }

    public List<InstancePO> head() {
        List<InstancePO> values = new ArrayList<>(id2instance.values());
        return values.subList(0, Math.min(100, values.size()));
    }

    @Override
    public List<Long> getAliveIds(long tenantId, Collection<Long> ids) {
        return NncUtils.filter(ids, id -> {
            InstancePO instanceArrayPO = id2instance.get(id);
            return instanceArrayPO != null && instanceArrayPO.getTenantId() == tenantId;
        });
    }

    @Override
    public int updateSyncVersion(List<VersionPO> versions) {
        for (VersionPO version : versions) {
            InstancePO instancePO = id2instance.get(version.id());
            if(instancePO == null) {
                instancePO = NncUtils.requireNonNull(removed.get(version.id()));
            }
            instancePO.setSyncVersion(version.version());
        }
        return versions.size();
    }

    @Override
    public List<InstancePO> scan(long tenantId, Collection<ScanQuery> queries) {
        Set<InstancePO> uniqueResult = new IdentitySet<>();
        for (ScanQuery query : queries) {
            uniqueResult.addAll(scan(tenantId, query));
        }
        List<InstancePO> result = new ArrayList<>(uniqueResult);
        result.sort(Comparator.comparingLong(InstancePO::getId));
        return result;
    }

    @Override
    public List<Long> selectVersions(List<Long> ids) {
        return NncUtils.map(ids, id -> id2instance.get(id).getVersion());
    }

    @Override
    public List<Version> selectRootVersions(long tenantId, List<Long> ids) {
        return ids.stream().map(id2instance::get)
                .filter(Objects::nonNull)
                .map(InstancePO::getRootId)
                .map(id2instance::get)
                .filter(Objects::nonNull)
                .distinct()
                .map(i -> new Version(i.getId(), i.getVersion()))
                .collect(Collectors.toList());
    }

    private List<InstancePO> scan(long tenantId, ScanQuery query) {
        Collection<InstancePO> tail = id2instance.tailMap(query.getStartId()).values();
        List<InstancePO> result = new ArrayList<>();
        for (InstancePO instancePO : tail) {
            if(result.size() >= query.getLimit()) {
                break;
            }
            if(instancePO.getTenantId() == tenantId) {
                result.add(instancePO);
            }
        }
        return result;
    }
}
