package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

public class MemInstanceMapper implements InstanceMapper {

    private final Map<Long, InstancePO> id2instance = new TreeMap<>();
    private final Map<Long, List<InstancePO>> type2instances = new HashMap<>();
    private final Map<Long, ReadWriteLock> lockMap = new HashMap<>();

    @Override
    public List<InstancePO> selectByTypeIds(long tenantId, Collection<Long> typeIds, long start, long limit) {
        List<InstancePO> result = NncUtils.flatMapAndFilter(
                typeIds,
                typeId -> type2instances.getOrDefault(typeId, List.of()),
                i -> i.getTenantId() == tenantId
        );
        return result.subList((int) start, Math.min(result.size(), (int) (start + limit)));
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
            remove(version.id());
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
            instancePO.setSyncVersion(version.version());
        }
        return versions.size();
    }

    @Override
    public List<InstanceTitlePO> selectTitleByIds(long tenantId, Collection<Long> ids) {
        List<InstancePO> instancePOs = selectByIds(tenantId, ids);
        return NncUtils.map(instancePOs, i -> new InstanceTitlePO(i.getId(), i.getTitle()));
    }
}
