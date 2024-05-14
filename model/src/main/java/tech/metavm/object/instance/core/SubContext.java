package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ReferenceExtractor;
import tech.metavm.entity.Tree;
import tech.metavm.object.instance.persistence.ReferencePO;

import java.util.*;

public final class SubContext {
    private final Map<Long, Tree> trees = new HashMap<>();
    private final Set<ReferencePO> references = new HashSet<>();
    private final long appId;

    public SubContext(long appId) {
        this.appId = appId;
    }

    public Tree get(long id) {
        return trees.get(id);
    }

    public void add(@NotNull Tree tree) {
        if (trees.containsKey(tree.id()))
            throw new IllegalArgumentException("Tree " + tree.id() + " already exists in the HeadContext");
        trees.put(tree.id(), tree);
        new ReferenceExtractor(tree.openInput(), appId, references::add).visitMessage();
    }

    public Set<ReferencePO> getReferences() {
        return references;
    }

    public void clear() {
        trees.clear();
        references.clear();
    }

    Collection<Tree> trees() {
        return Collections.unmodifiableCollection(trees.values());
    }

}
