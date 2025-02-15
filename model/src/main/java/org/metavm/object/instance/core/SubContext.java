package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SubContext {
    public static final Logger logger = LoggerFactory.getLogger(SubContext.class);
    private final Map<Long, Tree> trees = new HashMap<>();
    private boolean frozen;
    private final long appId;

    public SubContext(long appId) {
        this.appId = appId;
    }

    public Tree get(long id) {
        return trees.get(id);
    }

    public boolean tryAdd(@NotNull Tree tree) {
        if (trees.containsKey(tree.id()))
            return false;
        ensureNotFrozen();
        trees.put(tree.id(), tree);
        return true;
    }

    public void add(@NotNull Tree tree) {
        if(!tryAdd(tree))
            throw new IllegalArgumentException("Tree " + tree.id() + " already exists in the HeadContext");
    }

    public void clear() {
        ensureNotFrozen();
        trees.clear();
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
