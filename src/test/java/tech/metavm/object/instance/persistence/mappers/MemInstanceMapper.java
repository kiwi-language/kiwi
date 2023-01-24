package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

public class MemInstanceMapper implements InstanceMapper {

    private final NavigableMap<Long, InstancePO> id2instance = new TreeMap<>();
    private final Map<Long, List<InstancePO>> type2instances = new HashMap<>();
    private final Map<Long, InstancePO> removed = new HashMap<>();
    private final Map<Long, ReadWriteLock> lockMap = new HashMap<>();

    @Override
    public List<InstancePO> selectByTypeIds(long tenantId, Collection<ByTypeQuery> queries) {
        List<InstancePO> result = NncUtils.flatMap(
                queries,
                q -> queryByType(tenantId, q)
        );
        return NncUtils.deduplicateAndSort(result, Comparator.comparingLong(InstancePO::getId));
    }

    private List<InstancePO> queryByType(long tenantId, ByTypeQuery query) {
        List<InstancePO> result = NncUtils.filter(
                type2instances.get(query.getTypeId()),
                instancePO -> instancePO.getTenantId() == tenantId
        );
        return result.subList(0, Math.min((int) query.getLimit(), result.size()));
    }

    @Override
    public List<InstancePO> selectByIds(long tenantId, Collection<Long> ids) {
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
    }

    private void remove(long id) {
        InstancePO instanceArrayPO = id2instance.remove(id);
        if(instanceArrayPO == null) {
            throw new InternalException("Instance " + id + " does not exist");
        }
        type2instances.get(instanceArrayPO.getTypeId()).removeIf(i -> i.getId().equals(instanceArrayPO.getId()));
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
    public List<InstanceTitlePO> selectTitleByIds(long tenantId, Collection<Long> ids) {
        List<InstancePO> instancePOs = selectByIds(tenantId, ids);
        return NncUtils.map(instancePOs, i -> new InstanceTitlePO(i.getId(), i.getTitle()));
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
