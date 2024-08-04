package org.metavm.entity;

import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ContextDifference {

    private static final Logger logger = LoggerFactory.getLogger(ContextDifference.class);

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

    public void diffTrees(Collection<Tree> head, Collection<Tree> buffer) {
        try (var ignored = ContextUtil.getProfiler().enter("Difference.diff")) {
            NncUtils.forEachPair(head, buffer, this::diffTree);
        }
    }

    public void diffEntities(Collection<Tree> head, Collection<Tree> buffered) {
        try(var ignored = ContextUtil.getProfiler().enter("ContextDifference.diffEntities")) {
            NncUtils.forEachPair(head, buffered, this::diffEntity);
        }
    }

    private void diffTree(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            return;
        if (t1 == null)
            treeChanges.addInsert(buildInstancePO(t2));
        else if (t2 == null)
            treeChanges.addDelete(buildInstancePO(t1));
        else if (!Arrays.equals(t1.data(), t2.data()))
            treeChanges.addUpdate(buildInstancePO(t2));
    }

    public void diffEntity(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            return;
        if (t1 == null) {
            getInstanceIds(t2).forEach(id ->
                    entityChange.addInsert(new VersionRT(appId, id.getId(), t2.version())));
        } else if (t2 == null) {
            getInstanceIds(t1).forEach(id -> {
                entityChange.addDelete(new VersionRT(appId, id.getId(), t1.version() + 1));
            });

        } else if (!Arrays.equals(t1.data(), t2.data())) {
            NncUtils.forEachPair(getSubTrees(t1), getSubTrees(t2), (s1, s2) -> {
                if (s1 == null && s2 == null)
                    return;
                if (s1 == null)
                    entityChange.addInsert(new VersionRT(appId, s2.getId(), t2.version()));
                else if (s2 == null)
                    entityChange.addDelete(new VersionRT(appId, s1.getId(), t2.version()));
                else if (!s1.equals(s2))
                    entityChange.addUpdate(new VersionRT(appId, s2.getId(), t2.version()));
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
        var bout = new ByteArrayOutputStream();
        new StreamCopier(new ByteArrayInputStream(tree), bout) {
            @Override
            public void visitVersion(long version) {
                output.writeLong(version + 1);
            }
        }.visitGrove();
        return bout.toByteArray();
    }

    private List<Subtree> getSubTrees(Tree tree) {
        var subTrees = new ArrayList<Subtree>();
        new SubtreeExtractor(tree.openInput(), subTrees::add).visitGrove();
        return subTrees;
    }

    private List<DiffId> getInstanceIds(Tree tree) {
        var ids = new ArrayList<DiffId>();
        new StreamVisitor(tree.openInput()) {

            @Override
            public void visitInstanceBody(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
                var id =  PhysicalId.of(treeId, nodeId, typeOrTypeKey);
                var oldId = oldTreeId != -1L ? PhysicalId.of(oldTreeId, oldNodeId, typeOrTypeKey) : null;
                ids.add(new DiffId(id, oldId, useOldId));
                super.visitInstanceBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeOrTypeKey);
            }

        }.visitGrove();
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
