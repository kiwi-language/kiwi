package tech.metavm.entity;

import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.ChangeList;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemInstanceStore implements IInstanceStore {

    private final Map<Long, InstancePO> map = new HashMap<>();
    private final Map<Long, List<InstancePO>> typeIdToInstances = new HashMap<>();
    private final MemIndexItemMapper indexItemMapper;

    public MemInstanceStore(MemIndexItemMapper indexItemMapper) {
        this.indexItemMapper = indexItemMapper;
    }

    public MemInstanceStore() {
        this.indexItemMapper = new MemIndexItemMapper();
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        diff.deletes().forEach(this::remove);
        diff.inserts().forEach(this::add);
        diff.updates().forEach(this::remove);
        diff.updates().forEach(this::add);
    }

    private void add(InstancePO instancePO) {
        NncUtils.requireNonNull(instancePO.getId());
        NncUtils.requireNonNull(instancePO.getTypeId());
        NncUtils.requireNull(map.get(instancePO.getId()), "Duplicate instance id: " + instancePO.getId());
        checkInstanceId(instancePO);
        map.put(instancePO.getId(), instancePO);
        typeIdToInstances.computeIfAbsent(instancePO.getTypeId(), k -> new ArrayList<>())
                .add(instancePO);
    }

    private void checkInstanceId(InstancePO instancePO) {
        if(instancePO instanceof InstanceArrayPO) {
            if(!TypeCategory.ARRAY.idRangeContains(instancePO.getId())) {
                throw new InternalException("Array instance id not in array region. instance: " + instancePO);
            }
        }
    }

    private void remove(InstancePO instancePO) {
        NncUtils.requireNonNull(instancePO.getId());
        NncUtils.requireNonNull(instancePO.getTypeId());
        typeIdToInstances.get(instancePO.getTypeId()).removeIf(
                inst -> Objects.equals(instancePO.getId(), inst.getId())
        );
        map.remove(instancePO.getId());
    }

    @Override
    public List<Long> selectByKey(IndexKeyPO key, InstanceContext context) {
        NncUtils.requireNonNull(indexItemMapper, "indexItemMapper required");
        return NncUtils.map(
                indexItemMapper.selectByKeys(context.getTenantId(), List.of(key)),
                IndexItemPO::getInstanceId
        );
    }

    @Override
    public List<InstancePO> load(StoreLoadRequest request, InstanceContext context) {
        return NncUtils.mapAndFilter(request.ids(), map::get, Objects::nonNull);
    }

    public InstancePO get(long id) {
        return map.get(id);
    }

    @Override
    public List<InstancePO> getByTypeIds(Collection<Long> typeIds,
                                         long startIdExclusive,
                                         long limit,
                                         InstanceContext context) {
        List<InstancePO> instances = NncUtils.flatMap(
                typeIds,
                typeIdToInstances::get
        );

        instances = NncUtils.filter(instances, i -> i.getId() > startIdExclusive);
        instances.sort(Comparator.comparingLong(InstancePO::getId));
        instances = instances.subList(0, Math.min(instances.size(), (int) limit));

        return NncUtils.map(
                instances,
                EntityUtils::copyPojo
        );
    }

    @Override
    public boolean updateSyncVersion(List<VersionPO> versions) {
        boolean allSuccessful = true;
        for (VersionPO version : versions) {
            InstancePO instancePO = map.get(version.id());
            if(instancePO != null) {
                if (instancePO.getSyncVersion() < version.version()) {
                    instancePO.setSyncVersion(version.version());
                } else {
                    allSuccessful = false;
                }
            }
        }
        return allSuccessful;
    }

    public MemIndexItemMapper getIndexItemMapper() {
        return indexItemMapper;
    }

    public void clear() {
        map.clear();
        typeIdToInstances.clear();
        indexItemMapper.clear();
    }

}
