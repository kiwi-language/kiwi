package tech.metavm.entity;

import tech.metavm.object.instance.BaseInstanceStore;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.ChangeList;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

public class MemInstanceStore extends BaseInstanceStore {

    private final Map<Long, InstancePO> map = new HashMap<>();
    private final Map<Long, List<InstancePO>> typeIdToInstances = new HashMap<>();
    private final MemIndexEntryMapper indexEntryMapper;
    private final Map<Long, Set<ReferencePO>> referenceMap = new HashMap<>();

    public MemInstanceStore(MemIndexEntryMapper indexItemMapper) {
        this.indexEntryMapper = indexItemMapper;
    }

    public MemInstanceStore() {
        this.indexEntryMapper = new MemIndexEntryMapper();
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        diff.deletes().forEach(this::remove);
        diff.inserts().forEach(this::add);
        diff.updates().forEach(this::remove);
        diff.updates().forEach(this::add);
    }

    public void addIndex(long tenantId, IndexKeyPO key, long instanceId) {
        indexEntryMapper.batchInsert(List.of(new IndexEntryPO(tenantId, key, instanceId)));
    }

    @Override
    public void saveReferences(ChangeList<ReferencePO> refChanges) {
        refChanges.apply(
                this::addRefs,
                ref -> {},
                this::removeRefs
        );
    }

    private void addRefs(List<ReferencePO> refs) {
        refs.forEach(this::addRef);
    }

    private void removeRefs(List<ReferencePO> refs) {
        refs.forEach(this::removeRef);
    }

    private void addRef(ReferencePO ref) {
        if(!referenceMap.computeIfAbsent(ref.getTargetId(), k->new HashSet<>()).add(ref)) {
            throw new InternalException(ref + " already exists");
        }
    }

    private void removeRef(ReferencePO ref) {
        if(!referenceMap.computeIfAbsent(ref.getTargetId(), k->new HashSet<>()).remove(ref)) {
            throw new InternalException(ref + " does not exist");
        }
    }

    @Override
    public Set<Long> getStronglyReferencedIds(long tenantId, Set<Long> ids, Set<Long> excludedSourceIds) {
        return NncUtils.filterAndMapUnique(
                ids,
                id -> NncUtils.anyMatch(
                        referenceMap.get(id),
                        ref -> ref.getKind() == ReferenceKind.STRONG.code() &&
                                !excludedSourceIds.contains(ref.getSourceId())
                ),
                Function.identity()
        );
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
    public List<Long> selectByKey(IndexKeyPO key, IInstanceContext context) {
        NncUtils.requireNonNull(indexEntryMapper, "indexItemMapper required");
        return NncUtils.map(
                indexEntryMapper.selectByKeys(context.getTenantId(), List.of(key)),
                IndexEntryPO::getInstanceId
        );
    }

    @Override
    public List<Long> query(InstanceIndexQuery query, IInstanceContext context) {
        return NncUtils.map(
                indexEntryMapper.query(query.toPO(context.getTenantId())),
                IndexEntryPO::getInstanceId
        );
    }

    @Override
    public List<Long> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context) {
        Set<ReferencePO> refs = referenceMap.get(targetId);
        if(NncUtils.isEmpty(refs)) {
            return List.of();
        }
        List<Long> ids = NncUtils.map(refs, ReferencePO::getSourceId);
        return NncUtils.filterAndSortAndLimit(
                ids,
                id -> id > startIdExclusive,
                Long::compare,
                limit
        );
    }

    @Override
    protected List<InstancePO> loadInternally(StoreLoadRequest request, IInstanceContext context) {
        return NncUtils.mapAndFilter(
                request.ids(),
                id -> NncUtils.get(map.get(id), EntityUtils::copyPojo),
                Objects::nonNull
        );
    }

    @Override
    public Set<Long> getAliveInstanceIds(long tenantId, Set<Long> instanceIds) {
        return NncUtils.filterAndMapUnique(
                instanceIds,
                id -> {
                    InstancePO instancePO = map.get(id);
                    return instancePO != null &&
                            (instancePO.getTenantId() == tenantId || instancePO.getTenantId() == -1L);
                },
                Function.identity()
        );
    }

    public InstancePO get(long id) {
        return map.get(id);
    }

    @Override
    public List<InstancePO> getByTypeIds(Collection<Long> typeIds,
                                         long startIdExclusive,
                                         long limit,
                                         IInstanceContext context) {
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

    public MemIndexEntryMapper getIndexEntryMapper() {
        return indexEntryMapper;
    }

    public void clear() {
        map.clear();
        typeIdToInstances.clear();
        indexEntryMapper.clear();
    }

}
