package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ReferenceExtractor;
import org.metavm.entity.Tree;
import org.metavm.object.instance.persistence.ReferencePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class SubContext {
    public static final Logger logger = LoggerFactory.getLogger(SubContext.class);
    private final Map<Long, Tree> trees = new HashMap<>();
    private final Set<ReferencePO> references = new HashSet<>();
    private boolean frozen;
    private final long appId;

    public SubContext(long appId) {
        this.appId = appId;
    }

    public Tree get(long id) {
        return trees.get(id);
    }

    public boolean tryAdd(@NotNull Tree tree) {
        ensureNotFrozen();
        if (trees.containsKey(tree.id()))
            return false;
        trees.put(tree.id(), tree);
        new ReferenceExtractor(tree.openInput(), appId, references::add).visitGrove();
        return true;
    }

    public void add(@NotNull Tree tree) {
        if(!tryAdd(tree))
            throw new IllegalArgumentException("Tree " + tree.id() + " already exists in the HeadContext");
    }

    public Set<ReferencePO> getReferences() {
        return references;
    }

    public void clear() {
        ensureNotFrozen();
        trees.clear();
        references.clear();
    }

    Collection<Tree> trees() {
        return Collections.unmodifiableCollection(trees.values());
    }

    private void ensureNotFrozen() {
        if(frozen)
            throw new IllegalStateException("Head context is frozen");
    }

    public void freeze() {
        assert !frozen;
        frozen = true;
    }

    public void unfreeze() {
        assert frozen;
        frozen = false;
    }



}
