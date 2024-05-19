package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.VersionRT;
import tech.metavm.object.type.TypeOrTypeKey;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ContextDifference {

    private final long appId;
    private final EntityChange<VersionRT> entityChange = new EntityChange<>(VersionRT.class);
    private final EntityChange<ReferencePO> referenceChange = new EntityChange<>(ReferencePO.class);
    private final EntityChange<InstancePO> treeChanges = new EntityChange<>(InstancePO.class);
    private int numSubTreeAdded;
    private int numSubTreeRemoved;
    private int numSubTreeUpdated;

    public ContextDifference(long appId) {
        this.appId = appId;
    }

    public void diffReferences(Collection<ReferencePO> head, Collection<ReferencePO> buffer) {
        try (var ignored = ContextUtil.getProfiler().enter("Difference.diffReferences")) {
            NncUtils.forEachPair(head, buffer, Function.identity(), (r1, r2) -> {
                if (r1 == null) {
                    referenceChange.addInsert(r2);
                } else if (r2 == null) {
                    referenceChange.addDelete(r1);
                }
            });
        }
    }

    public void diff(Collection<Tree> head, Collection<Tree> buffer) {
        try (var entry = ContextUtil.getProfiler().enter("Difference.diff")) {
            NncUtils.forEachPair(head, buffer, this::diffOne);
            entry.addMessage("numSubTreeAdded", numSubTreeAdded);
            entry.addMessage("numSubTreeRemoved", numSubTreeRemoved);
            entry.addMessage("numSubTreeUpdated", numSubTreeUpdated);
        }
    }

    private void diffOne(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            return;
        if (t1 == null) {
            treeChanges.addInsert(buildInstancePO(t2));
            getInstanceIds(t2).forEach(id ->
                    entityChange.addInsert(new VersionRT(appId, id, t2.version())));
        } else if (t2 == null) {
            treeChanges.addDelete(buildInstancePO(t1));
            getInstanceIds(t1).forEach(id ->
                    entityChange.addDelete(new VersionRT(appId, id, t1.version() + 1)));
        } else if (!Arrays.equals(t1.data(), t2.data())) {
            treeChanges.addUpdate(buildInstancePO(t2));
            NncUtils.forEachPair(getSubTrees(t1), getSubTrees(t2), (s1, s2) -> {
                if (s1 == null && s2 == null)
                    return;
                if (s1 == null)
                    entityChange.addInsert(new VersionRT(appId, s2.id(), t2.version()));
                else if (s2 == null)
                    entityChange.addDelete(new VersionRT(appId, s1.id(), t2.version()));
                else if (!s1.equals(s2))
                    entityChange.addUpdate(new VersionRT(appId, s2.id(), t2.version()));
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

    private List<Subtree> getSubTrees(Tree tree) {
        var subTrees = new ArrayList<Subtree>();
        new SubtreeExtractor(tree.openInput(), subTrees::add).visitMessage();
        return subTrees;
    }

    private List<Id> getInstanceIds(Tree tree) {
        var ids = new ArrayList<Id>();
        new StreamVisitor(tree.openInput()) {

            @Override
            public void visitRecordBody(long nodeId, TypeOrTypeKey typeOrTypeKey) {
                ids.add(PhysicalId.of(getTreeId(), nodeId, typeOrTypeKey));
                super.visitRecordBody(nodeId, typeOrTypeKey);
            }

        }.visitMessage();
        return ids;
    }

    public EntityChange<ReferencePO> getReferenceChange() {
        return referenceChange;
    }

    public EntityChange<InstancePO> getTreeChanges() {
        return treeChanges;
    }

    public EntityChange<VersionRT> getEntityChange() {
        return entityChange;
    }

    public int getNumSubTreeAdded() {
        return numSubTreeAdded;
    }

    public int getNumSubTreeRemoved() {
        return numSubTreeRemoved;
    }

    public int getNumSubTreeUpdated() {
        return numSubTreeUpdated;
    }
}
