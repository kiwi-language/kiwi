package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.TargetPO;
import tech.metavm.util.DiffUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class MemReferenceMapper implements ReferenceMapper {

    private final Map<String, List<ReferencePO>> targetWithKindMap = new HashMap<>();
    private final Map<String, List<ReferencePO>> targetWithFieldMap = new HashMap<>();
    private final Map<Id, List<ReferencePO>> targetMap = new HashMap<>();
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
    public List<ReferencePO> selectByTargetId(long appId, byte[] targetId, long startIdExclusive, long limit) {
        List<ReferencePO> refs = targetMap.get(Id.fromBytes(targetId));
        if (NncUtils.isEmpty(refs)) {
            return List.of();
        }
        return NncUtils.filterAndSortAndLimit(
                refs,
                ref -> ref.getSourceTreeId() > startIdExclusive,
                Comparator.comparingLong(ReferencePO::getSourceTreeId),
                limit
        );
    }

    @Override
    public @Nullable ReferencePO selectFirstStrongReference(long appId,
                                                            Collection<byte[]> targetIds,
                                                            Collection<Long> excludedSourceIds) {
        var excludedSourceIdSet = new HashSet<>(excludedSourceIds);
        List<String> keys = NncUtils.map(targetIds, id -> appId + "-" + Id.fromBytes(id) + "-1");
        return keys.stream()
                .map(key -> NncUtils.find(
                        targetWithKindMap.get(key),
                        ref -> !excludedSourceIdSet.contains(ref.getSourceTreeId())
                ))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public List<ReferencePO> selectAllStrongReferences(long appId, Collection<byte[]> ids, Collection<Long> excludedSourceIds) {
        var excludedSourceIdSet = new HashSet<>(excludedSourceIds);
        List<String> keys = NncUtils.map(ids, id -> appId + "-" + Id.fromBytes(id) + "-1");
        return NncUtils.flatMapAndFilter(
                keys,
                key -> NncUtils.filter(
                        targetWithKindMap.get(key),
                        ref -> !excludedSourceIdSet.contains(ref.getSourceTreeId())
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
        ref = DiffUtils.copyPojo(ref);
        if (!references.add(ref)) {
            throw new RuntimeException(ref + " already exists");
        }
        targetWithKindMap.computeIfAbsent(ref.targetKeyWithKind(), k -> new ArrayList<>()).add(ref);
        targetWithFieldMap.computeIfAbsent(ref.targetKeyWithField(), k -> new ArrayList<>()).add(ref);
        targetMap.computeIfAbsent(ref.getTargetInstanceId(), k -> new ArrayList<>()).add(ref);
    }

    private void remove(ReferencePO ref) {
        if (!references.remove(ref)) {
            throw new RuntimeException(ref + " does not exist");
        }
        targetWithKindMap.get(ref.targetKeyWithKind()).remove(ref);
        targetWithFieldMap.get(ref.targetKeyWithField()).remove(ref);
        targetMap.get(ref.getTargetInstanceId()).remove(ref);
    }

    public MemReferenceMapper copy() {
        var copy = new MemReferenceMapper();
        copy.references.addAll(references);
        copy.targetWithKindMap.putAll(targetWithKindMap);
        copy.targetWithFieldMap.putAll(targetWithFieldMap);
        copy.targetMap.putAll(targetMap);
        return copy;
    }
}
