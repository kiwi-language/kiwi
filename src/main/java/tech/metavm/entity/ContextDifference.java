package tech.metavm.entity;

import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;

import java.util.*;

public class ContextDifference {

    private final Map<Class<? extends Entity>, EntityChange<?>> changeMap  = new HashMap<>();

    private final EntityChange<InstancePO> entityChange = new EntityChange<>(InstancePO.class);
    private final Map<Class<? extends Value>, ValueChange<?>> valueChangeMap = new HashMap<>();

    public void diff(Collection<InstancePO> head, Collection<InstancePO> buffer) {
        List<Pair<InstancePO>> pairs = NncUtils.buildPairs(head, buffer, InstancePO::getId);
        pairs.forEach(this::diffOne);
    }

    public void diffValues(Collection<Value> head, Collection<Value> buffer) {
        List<Pair<Value>> pairs = NncUtils.buildPairs(head, buffer);
        pairs.forEach(this::diffOneValue);
    }

    private void diffOneValue(Pair<? extends Value> pair) {
        Class<? extends Value> valueType = EntityUtils.getValueType(pair.any());
        diffOneValueHelper(valueType, pair);
    }

    private <T extends Value> void diffOneValueHelper(Class<T> valueType, Pair<? extends Value> pair) {
        Pair<? extends T> castPair = pair.cast(valueType);
        if(castPair.first() != null && castPair.second() != null) {
            return;
        }
        ValueChange<T> change = getValueChange(valueType);
        if(castPair.first() != null) {
            change.addDelete(castPair.first());
        }
        else if(castPair.second() != null) {
            change.addInsert(castPair.second());
        }
    }

    private void diffOne(Pair<InstancePO> pair) {
        if(pair.first() == null) {
            entityChange.addToInsert(pair.second());
        }
        else if(pair.second() == null) {
            entityChange.addToDelete(pair.first());
        }
        else if(EntityUtils.isPojoDifferent(pair.first(), pair.second())) {
            entityChange.addToUpdate(pair.second());
        }
    }

//    private <S extends Entity> void diffOneHelper(Class<S> entityType, Pair<? extends Entity> pair) {
//        Pair<? extends S> castPair = pair.cast(entityType);
//        EntityChange<S> change = getChange(entityType);
//        if(castPair.first() == null) {
//            change.addToInsert(castPair.second());
//        }
//        else if(castPair.second() == null) {
//            change.addToDelete(castPair.first());
//        }
//        else if(EntityUtils.isPojoDifferent(castPair.first(), castPair.second())) {
//            change.addToUpdate(castPair.second());
//        }
//    }

//    @SuppressWarnings("unchecked")
//    private <S extends Entity> EntityChange<S> getChange(Class<S> entityType) {
//        EntityChange<?> entityChange = changeMap.computeIfAbsent(entityType, k -> new EntityChange<>(entityType));
//        if(!entityChange.getEntityType().equals(entityType)) {
//            throw new ClassCastException();
//        }
//        return (EntityChange<S>) entityChange;
//    }

    @SuppressWarnings("unchecked")
    private <S extends Value> ValueChange<S> getValueChange(Class<S> valueType) {
        return (ValueChange<S>) valueChangeMap.computeIfAbsent(valueType, k -> new ValueChange<>(valueType));
    }

    public Map<Class<? extends Entity>, EntityChange<?>> getChangeMap() {
        return changeMap;
    }

    public EntityChange<InstancePO> getEntityChange() {
        return entityChange;
    }

    public Map<Class<? extends Value>, ValueChange<?>> getValueChangeMap() {
        return valueChangeMap;
    }
}
