package tech.metavm.object.instance.core;

import tech.metavm.entity.ReferenceExtractor;
import tech.metavm.entity.Tree;
import tech.metavm.entity.Value;
import tech.metavm.object.instance.persistence.ReferencePO;

import java.util.*;

public final class SubContext {
    private final Set<Value> values = new LinkedHashSet<>();
    private final IdentityHashMap<Tree, Tree> entities = new IdentityHashMap<>();
    private final Map<Long, Tree> entityMap = new HashMap<>();
    private final Set<ReferencePO> references = new HashSet<>();
    private final long tenantId;

    public SubContext(long tenantId) {
        this.tenantId = tenantId;
    }

    public Tree get(long id) {
        return entityMap.get(id);
    }

    public void add(Tree tree) {
        if(entities.containsKey(tree))
            return;
        Objects.requireNonNull(tree);
        Tree existing = entityMap.remove(tree.id());
        if (existing != null) {
            entities.remove(existing);
        }
        entityMap.put(tree.id(), tree);
        entities.put(tree, tree);
        new ReferenceExtractor(tree.openInput(), tenantId, references::add).visitMessage();
    }

    public Set<ReferencePO> getReferences() {
        return references;
    }

    public void addValue(Value value) {
        values.add(value);
    }

    public Collection<Value> values() {
        return values;
    }

    public Collection<Tree> entities() {
        return entities.values();
    }

    public void clear() {
        entities.clear();
        entityMap.clear();
        references.clear();
    }

    public boolean remove(Tree entity) {
        var removed = entities.remove(entity);
        if (removed != null) {
            entityMap.remove(removed.id());
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(Value value) {
        return values.remove(value);
    }

    Collection<Tree> trees() {
        return Collections.unmodifiableCollection(entities.values());
    }

}
