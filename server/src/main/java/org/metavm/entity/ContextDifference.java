package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.Refcount;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.PersistenceUtils;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class ContextDifference {

    private static final Logger logger = LoggerFactory.getLogger(ContextDifference.class);

    private final long appId;
    private final EntityChange<VersionRT> entityChange = new EntityChange<>(VersionRT.class);
    private final EntityChange<InstancePO> treeChanges = new EntityChange<>(InstancePO.class);
    private final Map<Id, Refcount> refcounts = new HashMap<>();
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

    public void diffReferences(Collection<Tree> head, Collection<Tree> buffered) {
        Utils.forEachPair(head, buffered, this::diffRef);
        refcounts.entrySet().removeIf(e -> e.getValue().getCount() == 0);
    }

    private void diffTree(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 == null && t2 == null)
            throw new NullPointerException("Both trees are null");
        if (t1 == null) {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is created", t2.id());
            treeChanges.addInsert(PersistenceUtils.buildInstancePO(appId, t2));
        } else if (t2 == null) {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is deleted", t1.id());
            treeChanges.addDelete(PersistenceUtils.buildInstancePO(appId, t1));
        } else if (!Arrays.equals(t1.data(), t2.data())) {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is modified", t1.id());
            treeChanges.addUpdate(PersistenceUtils.buildInstancePO(appId, t2));
        }
        else {
            if (DebugEnv.traceDifference)
                logger.trace("Tree {} is unchanged", t1.id());
        }
    }

    private void diffRef(@Nullable Tree t1, @Nullable Tree t2) {
        if (t1 != null)
            removeRefcounts(getRefcounts(t1));
        if (t2 != null)
            addRefcounts(getRefcounts(t2));
    }

    private void addRefcounts(List<Refcount> rc) {
        rc.forEach(c -> refcounts.computeIfAbsent(c.getTarget(), Refcount::new).inc(c.getCount()));
    }

    private void removeRefcounts(List<Refcount> rc) {
        rc.forEach(c -> refcounts.computeIfAbsent(c.getTarget(), Refcount::new).dec(c.getCount()));
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

    private List<Subtree> getSubTrees(Tree tree) {
        var subTrees = new ArrayList<Subtree>();
        new SubtreeExtractor(tree.openInput(), subTrees::add).visitGrove();
        return subTrees;
    }

    private List<DiffId> getInstanceIds(Tree tree) {
        var ids = new ArrayList<DiffId>();
        new StreamVisitor(tree.openInput()) {

            @Override
            public void visitInstanceBody(long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey, int refcount) {
                var id =  PhysicalId.of(treeId, nodeId);
                ids.add(new DiffId(id, -1));
                super.visitInstanceBody(treeId, nodeId, typeOrTypeKey, refcount);
            }

            @Override
            public void visitEntityBody(int tag, Id id, int refcount) {
                ids.add(new DiffId(id, tag));
                super.visitEntityBody(tag, id, refcount);
            }
        }.visitGrove();
        return ids;
    }

    private List<Refcount> getRefcounts(Tree tree) {
        var visited = new HashSet<Id>();
        var refcounts = new ArrayList<Refcount>();
        new StreamVisitor(tree.openInput()) {

            @Override
            public void visitReference() {
                var id = readId();
                if (visited.add(id))
                    refcounts.add(new Refcount(id, 1));

            }
        }.visitGrove();
        return refcounts;
    }

    public EntityChange<InstancePO> getTreeChanges() {
        return treeChanges;
    }

    public EntityChange<VersionRT> getEntityChange() {
        return entityChange;
    }

    public Collection<Refcount> getRefcountChange() {
        return Collections.unmodifiableCollection(refcounts.values());
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
