package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.persistence.InstancePO;
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

public class ContextDifference {

    private static final Logger logger = LoggerFactory.getLogger(ContextDifference.class);

    private final long appId;
    private final EntityChange<VersionRT> entityChange = new EntityChange<>(VersionRT.class);
    private final EntityChange<InstancePO> treeChanges = new EntityChange<>(InstancePO.class);
    private int numSubTreeAdded;
    private int numSubTreeRemoved;
    private int numSubTreeUpdated;

    public ContextDifference(long appId) {
        this.appId = appId;
    }

    public void diffTrees(Collection<Tree> head, Collection<Tree> buffer) {
        try (var ignored = ContextUtil.getProfiler().enter("Difference.diff")) {
            Utils.forEachPair(head, buffer, this::diffTree);
        }
    }

    public void diffEntities(Collection<Tree> head, Collection<Tree> buffered) {
        try(var ignored = ContextUtil.getProfiler().enter("ContextDifference.diffEntities")) {
            Utils.forEachPair(head, buffered, this::diffEntity);
        }
    }

    private void diffTree(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            throw new NullPointerException("Both trees are null");
        if (t1 == null) {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is created", t2.id());
            treeChanges.addInsert(buildInstancePO(t2));
        } else if (t2 == null) {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is deleted", t1.id());
            treeChanges.addDelete(buildInstancePO(t1));
        } else if (!Arrays.equals(t1.data(), t2.data())) {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is modified", t1.id());
            treeChanges.addUpdate(buildInstancePO(t2));
        }
        else {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is unchanged", t1.id());
        }
    }

    public void diffEntity(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            return;
        if (t1 == null) {
            getInstanceIds(t2).forEach(s -> {
                if (DebugEnv.traceDifference)
                    logger.trace("Entity {} created, treeId: {}", s.id(), s.id().getTreeId());
                entityChange.addInsert(new VersionRT(appId, s.id(), t2.version(), s.entityTag()));
            });
        } else if (t2 == null) {
            getInstanceIds(t1).forEach(s -> {
                if (DebugEnv.traceDifference)
                    logger.trace("Entity {} deleted", s.id());
                entityChange.addDelete(new VersionRT(appId, s.id(), t1.version() + 1, s.entityTag()));
            });

        } else if (!Arrays.equals(t1.data(), t2.data())) {
            Utils.forEachPair(getSubTrees(t1), getSubTrees(t2), (s1, s2) -> {
                if (s1 == null && s2 == null)
                    return;
                if (s1 == null) {
                    if (DebugEnv.traceDifference)
                        logger.trace("Entity {} created", s2.id());
                    entityChange.addInsert(new VersionRT(appId, s2.id(), t2.version(), s2.entityTag()));
                } else if (s2 == null) {
                    if (DebugEnv.traceDifference)
                        logger.trace("Entity {} deleted", s1.id());
                    entityChange.addDelete(new VersionRT(appId, s1.id(), t2.version(), s1.entityTag()));
                } else if (!s1.equals(s2)) {
                    if (DebugEnv.traceDifference)
                        logger.trace("Entity {} updated", s1.id());
                    entityChange.addUpdate(new VersionRT(appId, s2.id(), t2.version(), s2.entityTag()));
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
            public void visitInstanceBody(long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
                var id =  PhysicalId.of(treeId, nodeId);
                ids.add(new DiffId(id, -1));
                super.visitInstanceBody(treeId, nodeId, typeOrTypeKey);
            }

            @Override
            public void visitEntityBody(int tag, Id id) {
                ids.add(new DiffId(id, tag));
                super.visitEntityBody(tag, id);
            }
        }.visitGrove();
        return ids;
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
