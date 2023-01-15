package tech.metavm.entity;

import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;

import java.util.*;
import java.util.function.Function;

public class ContextDifference {

    private final Map<Class<? extends Entity>, EntityChange<?>> changeMap  = new HashMap<>();

    private final EntityChange<InstancePO> entityChange = new EntityChange<>(InstancePO.class);
    private final EntityChange<ReferencePO> referenceChange = new EntityChange<>(ReferencePO.class);
    private final Map<Class<? extends Value>, ValueChange<?>> valueChangeMap = new HashMap<>();
    private final IInstanceContext context;

    public ContextDifference(IInstanceContext context) {
        this.context = context;
    }

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
            referenceChange.addAllToInsert(extractRefs(pair.second()));
        }
        else if(pair.second() == null) {
            entityChange.addToDelete(pair.first());
            referenceChange.addAllToDelete(extractRefs(pair.first()));
        }
        else if(EntityUtils.isPojoDifferent(pair.first(), pair.second())) {
            List<Pair<ReferencePO>> refPairs = NncUtils.buildPairs(
                extractRefs(pair.first()),
                extractRefs(pair.second()),
                Function.identity()
            );
            for (Pair<ReferencePO> refPair : refPairs) {
                if(refPair.first() == null && refPair.second() != null) {
                    referenceChange.addToInsert(refPair.second());
                }
                else if(refPair.first() != null && refPair.second() == null) {
                    referenceChange.addToDelete(refPair.first());
                }
            }
            entityChange.addToUpdate(pair.second());
        }
    }

    private List<ReferencePO> extractRefs(InstancePO instancePO) {
        Type type = context.getType(instancePO.getTypeId());
        if(type instanceof ClassType classType) {
            return extractRefsForObject(instancePO, classType);
        }
        if(type instanceof ArrayType arrayType) {
            return extractRefsForArray((InstanceArrayPO) instancePO, arrayType);
        }
        return List.of();
    }

    private List<ReferencePO> extractRefsForObject(InstancePO instancePO, ClassType classType) {
        List<ReferencePO> refs = new ArrayList<>();
        for (Field field : classType.getFields()) {
            NncUtils.invokeIfNotNull(
                    convertToRefId(instancePO.get(field.getColumnName()), field.isReference()),
                    targetId -> refs.add(new ReferencePO(
                            instancePO.getTenantId(),
                            instancePO.getId(),
                            targetId,
                            field.getId(),
                            ReferenceKind.getFromType(field.getType()).code()
                    ))
            );
        }
        return refs;
    }

    private Long convertToRefId(Object fieldValue, boolean isRef) {
        if(fieldValue == null) {
            return null;
        }
        if(fieldValue instanceof IdentityPO identityPO){
            return identityPO.id();
        }
        if(isRef) {
            return (Long) fieldValue;
        }
        return null;
    }

    private List<ReferencePO> extractRefsForArray(InstanceArrayPO arrayPO, ArrayType arrayType) {
        List<ReferencePO> refs = new ArrayList<>();
        boolean isRefType = arrayType.getElementType().isReference();
        for (Object element : arrayPO.getElements()) {
            NncUtils.invokeIfNotNull(
                    convertToRefId(element, isRefType),
                    targetId -> refs.add(new ReferencePO(
                            arrayPO.getTenantId(),
                            arrayPO.getId(),
                            targetId,
                            -1L,
                            ReferenceKind.getFromType(arrayType.getElementType()).code()
                    ))
            );
        }
        return refs;
    }

    public EntityChange<ReferencePO> getReferenceChange() {
        return referenceChange;
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
