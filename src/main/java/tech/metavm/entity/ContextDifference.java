package tech.metavm.entity;

import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;

import java.util.*;

public class ContextDifference {

    private final Map<Class<?>, EntityChange> changeMap  = new HashMap<>();

    public void diff(List<Entity> head, List<Entity> buffer) {
        List<Pair<Entity>> pairs = NncUtils.buildEntityPairs(head, buffer);
        pairs.forEach(pair -> diffOne(pair.first(), pair.second()));
    }

    public void diffOne(Entity entity1, Entity entity2) {
        if(entity1 == null && entity2 == null) {
            return;
        }
        Class<?> entityType = EntityUtils.getEntityType(NncUtils.anyNoneNull(entity1, entity2).getClass());
        EntityChange change = changeMap.computeIfAbsent(entityType, (k) -> new EntityChange());
        if(entity1 == null) {
            change.addToInsert(entity2);
        }
        else if(entity2 == null) {
            change.addToDelete(entity1);
        }
        else if(!EntityUtils.pojoEquals(entity1, entity2)) {
            change.addToUpdate(entity2);
        }
    }

    public Map<Class<?>, EntityChange> getChangeMap() {
        return changeMap;
    }
}
