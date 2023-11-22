package tech.metavm.entity;

import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ContextDifference {

    private final Map<Class<? extends Entity>, EntityChange<?>> changeMap = new HashMap<>();

    private final EntityChange<InstancePO> entityChange = new EntityChange<>(InstancePO.class);
    private final EntityChange<ReferencePO> referenceChange = new EntityChange<>(ReferencePO.class);
    private final Map<Class<? extends Value>, ValueChange<?>> valueChangeMap = new HashMap<>();
    private final long tenantId;
    private final Function<Long, Long> getTypeId;

    public ContextDifference(long tenantId, Function<Long, Long> getTypeId) {
        this.tenantId = tenantId;
        this.getTypeId = getTypeId;
    }

    public void diffReferences(Collection<ReferencePO> head, Collection<ReferencePO> buffer) {
        NncUtils.forEachPair(head, buffer, Function.identity(), (r1, r2) -> {
            if (r1 == null) {
                referenceChange.addToInsert(r2);
            } else if (r2 == null) {
                referenceChange.addToDelete(r1);
            }
        });
    }

    public void diff(Collection<Tree> head, Collection<Tree> buffer) {
        NncUtils.forEachPair(new ArrayList<>(head), new ArrayList<>(buffer), this::diffOne);
    }

    private void diffOne(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            return;
        if (t1 == null) {
            getSubTrees(t2).forEach(subtree ->
                    entityChange.addToInsert(toInstancePO(t2, subtree)));
        } else if (t2 == null) {
            getSubTrees(t1).forEach(subtree ->
                    entityChange.addToDelete(toInstancePO(t1, subtree)));
        } else if (!Arrays.equals(t1.data(), t2.data())) {
            var firstSubtrees = getSubTrees(t1);
            var secondSubtrees = getSubTrees(t2);
            var ref = new Object() {
                boolean rootChanged;
            };
            var rootId = t1.id();
            var newVersion = t1.version() + 1;
            NncUtils.forEachPair(firstSubtrees, secondSubtrees, (s1, s2) -> {
                if (s1 == null && s2 == null)
                    return;
                if (s1 == null)
                    entityChange.addToInsert(toInstancePO(t2, s2));
                else if (s2 == null)
                    entityChange.addToDelete(toInstancePO(t1, s1));
                else if (!s1.equals(s2)) {
                    var updatePO = toInstancePO(t2, s2);
                    entityChange.addToUpdate(updatePO);
                    if (s2.id() == rootId) {
                        updatePO.setVersion(newVersion);
                        ref.rootChanged = true;
                    }
                }
            });
            if (!ref.rootChanged) {
                entityChange.addToUpdate(new InstancePO(tenantId, rootId, "",
                        getTypeId.apply(rootId), null,
                        -1L, -1L, rootId, newVersion, 0L));
            }
        }
    }

    public void diffValues(Collection<Value> head, Collection<Value> buffer) {
        List<Pair<Value>> pairs = NncUtils.buildPairs(head, buffer);
        pairs.forEach(this::diffOneValue);
    }

    private void diffOneValue(Pair<? extends Value> pair) {
        Class<? extends Value> valueType = EntityUtils.getValueType(pair.any());
        diffOneValueHelper(valueType, pair);
    }

    private <T extends Value> void diffOneValueHelper(Class<T> valueType, Pair<? extends Value> pair) {
        Pair<? extends T> castPair = pair.cast(valueType);
        if (castPair.first() != null && castPair.second() != null)
            return;
        ValueChange<T> change = getValueChange(valueType);
        if (castPair.first() != null) {
            change.addDelete(castPair.first());
        } else if (castPair.second() != null) {
            change.addInsert(castPair.second());
        }
    }

    private InstancePO toInstancePO(Tree tree, Subtree subTree) {
        var rootId = tree.id();
        var id = subTree.id();
        return new InstancePO(
                tenantId,
                id,
                "",
                getTypeId.apply(id),
                subTree.data(),
                subTree.parentId(),
                subTree.parentFieldId(),
                rootId,
                id == rootId ? tree.version() : 0L,
                0L
        );
    }

    private List<Subtree> getSubTrees(Tree tree) {
        List<Subtree> subTrees = new ArrayList<>();
        new SubtreeExtractor(tree.openInput(), subTrees::add).visitMessage();
        return subTrees;
    }

    public EntityChange<ReferencePO> getReferenceChange() {
        return referenceChange;
    }

    @SuppressWarnings("unchecked")
    private <S extends Value> ValueChange<S> getValueChange(Class<S> valueType) {
        return (ValueChange<S>) valueChangeMap.computeIfAbsent(valueType, k -> new ValueChange<>(valueType));
    }

    public Map<Class<? extends Entity>, EntityChange<?>> getChangeMap() {
        return changeMap;
    }

    public EntityChange<InstancePO> getEntityChange() {
        return entityChange;
    }

    public Map<Class<? extends Value>, ValueChange<?>> getValueChangeMap() {
        return valueChangeMap;
    }


}
