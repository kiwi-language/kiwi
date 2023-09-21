package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.TargetPO;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemReferenceMapper implements ReferenceMapper {

    private final Map<String, List<ReferencePO>> targetWithKindMap = new HashMap<>();
    private final Map<String, List<ReferencePO>> targetWithFieldMap = new HashMap<>();
    private final Map<Long, List<ReferencePO>> targetMap = new HashMap<>();
    private final Set<ReferencePO> references = new HashSet<>();

    @Override
    public List<ReferencePO> selectByTargetsWithKind(Collection<TargetPO> targets) {
        return NncUtils.flatMap(targets, t -> targetWithKindMap.get(t.keyWithKind()));
    }

    @Override
    public List<ReferencePO> selectByTargetsWithField(Collection<TargetPO> targets) {
        return NncUtils.flatMap(targets, t -> targetWithFieldMap.get(t.keyWithField()));
    }

    @Override
    public List<ReferencePO> selectByTargetId(long tenantId, long targetId, long startIdExclusive, long limit) {
        List<ReferencePO> refs = targetMap.get(targetId);
        if(NncUtils.isEmpty(refs)) {
            return List.of();
        }
        return NncUtils.filterAndSortAndLimit(
                refs,
                ref -> ref.getSourceId() > startIdExclusive,
                Comparator.comparingLong(ReferencePO::getSourceId),
                limit
        );
    }

    @Override
    public List<ReferencePO> selectFirstStrongReferences(long tenantId,
                                                         Collection<Long> ids,
                                                         Collection<Long> excludedSourceIds) {
        Set<Long> excludedSourceIdSet = new HashSet<>(excludedSourceIds);
        List<String> keys = NncUtils.map(ids, id -> tenantId + "-" + id + "-1");
        return NncUtils.mapAndFilter(
                keys,
                key -> NncUtils.find(
                        targetWithKindMap.get(key),
                        ref -> !excludedSourceIdSet.contains(ref.getSourceId())
                ),
                Objects::nonNull
        );
    }

    @Override
    public List<ReferencePO> selectAllStrongReferences(long tenantId, Collection<Long> ids, Collection<Long> excludedSourceIds) {
        Set<Long> excludedSourceIdSet = new HashSet<>(excludedSourceIds);
        List<String> keys = NncUtils.map(ids, id -> tenantId + "-" + id + "-1");
        return NncUtils.flatMapAndFilter(
                keys,
                key -> NncUtils.filter(
                        targetWithKindMap.get(key),
                        ref -> !excludedSourceIdSet.contains(ref.getSourceId())
                ),
                Objects::nonNull
        );
    }

    @Override
    public void batchInsert(Collection<ReferencePO> records) {
        records.forEach(this::add);
    }

    @Override
    public void batchUpdate(Collection<ReferencePO> records) {
        records.forEach(this::remove);
        records.forEach(this::add);
    }

    @Override
    public void batchDelete(Collection<ReferencePO> records) {
        records.forEach(this::remove);
    }

    private void add(ReferencePO ref) {
        ref = EntityUtils.copyPojo(ref);
        if(!references.add(ref)) {
            throw new RuntimeException(ref + " already exists");
        }
        targetWithKindMap.computeIfAbsent(ref.targetKeyWithKind(), k -> new ArrayList<>()).add(ref);
        targetWithFieldMap.computeIfAbsent(ref.targetKeyWithField(), k -> new ArrayList<>()).add(ref);
        targetMap.computeIfAbsent(ref.getTargetId(), k -> new ArrayList<>()).add(ref);
    }

    private void remove(ReferencePO ref) {
        if(!references.remove(ref)) {
            throw new RuntimeException(ref + " does not exist");
        }
        targetWithKindMap.get(ref.targetKeyWithKind()).remove(ref);
        targetWithFieldMap.get(ref.targetKeyWithField()).remove(ref);
        targetMap.get(ref.getTargetId()).remove(ref);
    }

}
