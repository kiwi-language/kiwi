package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemInstanceArrayMapper implements InstanceArrayMapper {

    private final Map<Long, InstanceArrayPO> id2instance = new TreeMap<>();
    private final Map<Long, List<InstanceArrayPO>> type2instances = new HashMap<>();

    @Override
    public List<InstanceArrayPO> selectByTypeIds(long tenantId, Collection<ByTypeQuery> queries) {
        List<InstanceArrayPO> result = NncUtils.flatMap(
                queries,
                query -> queryByType(tenantId, query)
        );
        return NncUtils.deduplicateAndSort(result, Comparator.comparingLong(InstancePO::getId));
    }

    private List<InstanceArrayPO> queryByType(long tenantId, ByTypeQuery query) {
        return NncUtils.filterAndLimit(
                type2instances.get(query.getTypeId()),
                i -> i.getTenantId() == tenantId,
                query.getLimit()
        );
    }

    @Override
    public List<InstanceArrayPO> selectByIds(long tenantId, Collection<Long> ids, int lockMode) {
        return NncUtils.mapAndFilter(
                ids,
                id2instance::get,
                i -> i.getTenantId() == tenantId
        );
    }

    @Override
    public void batchUpdate(Collection<InstanceArrayPO> records) {
        for (InstanceArrayPO record : records) {
            remove(record.getIdRequired());
            add(record);
        }
    }

    @Override
    public void batchInsert(Collection<InstanceArrayPO> records) {
        records.forEach(this::add);
    }

    @Override
    public void batchDelete(Collection<Long> ids) {
        ids.forEach(this::remove);
    }

    private void add(InstanceArrayPO instancePO) {
        instancePO = EntityUtils.copyPojo(instancePO);
        NncUtils.requireNull(id2instance.get(instancePO.getId()),
                "Instance with id " + instancePO.getId() + " already exists");
        id2instance.put(instancePO.getId(), instancePO);
        type2instances.computeIfAbsent(instancePO.getTypeId(), k -> new ArrayList<>()).add(instancePO);
    }

    private void remove(long id) {
        InstanceArrayPO instanceArrayPO = id2instance.remove(id);
        if(instanceArrayPO == null) {
            throw new InternalException("Instance " + id + " does not exist");
        }
        type2instances.get(instanceArrayPO.getTypeId()).removeIf(i -> i.getId().equals(instanceArrayPO.getId()));
    }

    @Override
    public List<InstanceArrayPO> head() {
        List<InstanceArrayPO> values = new ArrayList<>(id2instance.values());
        return values.subList(0, Math.min(100, values.size()));
    }

    @Override
    public List<Long> getAliveIds(long tenantId, Collection<Long> ids) {
        return NncUtils.filter(ids, id -> {
            InstanceArrayPO instanceArrayPO = id2instance.get(id);
            return instanceArrayPO != null && instanceArrayPO.getTenantId() == tenantId;
        });
    }
}
