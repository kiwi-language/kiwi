package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class EntityChange {
    private final List<Entity> toInserts = new ArrayList<>();
    private final List<Entity> toUpdate = new ArrayList<>();
    private final List<Entity> toDelete = new ArrayList<>();

    public void addToInsert(Entity entity) {
        toInserts.add(entity);
    }

    public void addToUpdate(Entity entity) {
        toUpdate.add(entity);
    }

    public void addToDelete(Entity entity) {
        toDelete.add(entity);
    }

    public void apply(EntityStore<?> store) {
        if(NncUtils.isNotEmpty(toInserts)) {
            store.batchInsert((List) toInserts);
        }
        if(NncUtils.isNotEmpty(toUpdate)) {
            store.batchUpdate((List) toUpdate);
        }
        if(NncUtils.isNotEmpty(toDelete)) {
            store.batchDelete((List) toDelete);
        }
    }

    public List<Entity> getToInserts() {
        return toInserts;
    }

    public List<Entity> getToUpdate() {
        return toUpdate;
    }

    public List<Entity> getToDelete() {
        return toDelete;
    }

    public boolean isEmpty() {
        return toDelete.isEmpty() && toUpdate.isEmpty() && toInserts.isEmpty();
    }

}
