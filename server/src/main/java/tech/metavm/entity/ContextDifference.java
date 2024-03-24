package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.VersionRT;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;

public class ContextDifference {

    private final Map<Class<? extends Entity>, EntityChange<?>> changeMap = new HashMap<>();

    private final EntityChange<VersionRT> entityChange = new EntityChange<>(VersionRT.class);
    private final EntityChange<ReferencePO> referenceChange = new EntityChange<>(ReferencePO.class);
    private final Map<Class<? extends Value>, ValueChange<?>> valueChangeMap = new HashMap<>();
    private final long appId;
    private final Function<Id, Id> getTypeId;
    private final EntityChange<InstancePO> treeChanges = new EntityChange<>(InstancePO.class);

    public ContextDifference(long appId, Function<Id, Id> getTypeId) {
        this.appId = appId;
        this.getTypeId = getTypeId;
    }

    public void diffReferences(Collection<ReferencePO> head, Collection<ReferencePO> buffer) {
        NncUtils.forEachPair(head, buffer, Function.identity(), (r1, r2) -> {
            if (r1 == null) {
                referenceChange.addInsert(r2);
            } else if (r2 == null) {
                referenceChange.addDelete(r1);
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
            treeChanges.addInsert(buildInstancePO(t2));
            getSubTrees(t2).forEach(subtree ->
                    entityChange.addInsert(new VersionRT(appId, subtree.id(), t2.version())));
        } else if (t2 == null) {
            treeChanges.addDelete(buildInstancePO(t1));
            getSubTrees(t1).forEach(subtree ->
                    entityChange.addDelete(new VersionRT(appId, subtree.id(), t1.version() + 1)));
        } else if (!Arrays.equals(t1.data(), t2.data())) {
            treeChanges.addUpdate(buildInstancePO(t2));
            var firstSubtrees = getSubTrees(t1);
            var secondSubtrees = getSubTrees(t2);
            NncUtils.forEachPair(firstSubtrees, secondSubtrees, (s1, s2) -> {
                if (s1 == null && s2 == null)
                    return;
                if (s1 == null)
                    entityChange.addInsert(new VersionRT(appId, s2.id(), t2.version()));
                else if (s2 == null)
                    entityChange.addDelete(new VersionRT(appId, s1.id(), t2.version()));
                else if (!s1.equals(s2)) {
                    entityChange.addUpdate(new VersionRT(appId, s2.id(), t2.version()));
                }
            });
        }
    }

    private InstancePO buildInstancePO(Tree tree) {
        return new InstancePO(
                appId,
                tree.id(),
                incVersion(tree.data()),
                tree.version() + 1,
                0L,
                tree.nextNodeId()
        );
    }

    private byte[] incVersion(byte[] tree) {
        var bin = new ByteArrayInputStream(tree);
        var input = new InstanceInput(bin);
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeLong(input.readLong() + 1);
        var newHeadBytes = bout.toByteArray();
        var oldHeadLen = calcHeadLength(tree);
        var length = newHeadBytes.length - oldHeadLen + tree.length;
        var newTree = new byte[length];
        System.arraycopy(newHeadBytes, 0, newTree, 0, newHeadBytes.length);
        System.arraycopy(tree, oldHeadLen, newTree, newHeadBytes.length, tree.length - oldHeadLen);
        return newTree;
    }

    private int calcHeadLength(byte[] tree) {
        var bin = new ByteArrayInputStream(tree);
        var input = new InstanceInput(bin);
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeLong(input.readLong());
        return bout.toByteArray().length;
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
        if (castPair.first() != null)
            change.addDelete(castPair.first());
        else if (castPair.second() != null)
            change.addInsert(castPair.second());
    }

    private List<Subtree> getSubTrees(Tree tree) {
        List<Subtree> subTrees = new ArrayList<>();
        new SubtreeExtractor(tree.openInput(), subTrees::add).visitMessage();
        return subTrees;
    }

    public EntityChange<ReferencePO> getReferenceChange() {
        return referenceChange;
    }

    public EntityChange<InstancePO> getTreeChanges() {
        return treeChanges;
    }

    @SuppressWarnings("unchecked")
    private <S extends Value> ValueChange<S> getValueChange(Class<S> valueType) {
        return (ValueChange<S>) valueChangeMap.computeIfAbsent(valueType, k -> new ValueChange<>(valueType));
    }

    public Map<Class<? extends Entity>, EntityChange<?>> getChangeMap() {
        return changeMap;
    }

    public EntityChange<VersionRT> getEntityChange() {
        return entityChange;
    }

    public Map<Class<? extends Value>, ValueChange<?>> getValueChangeMap() {
        return valueChangeMap;
    }


}
