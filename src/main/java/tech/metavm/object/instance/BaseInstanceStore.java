package tech.metavm.object.instance;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseInstanceStore implements IInstanceStore {

    @Override
    public final List<InstancePO> load(StoreLoadRequest request, IInstanceContext context) {
        List<InstancePO> instancePOs = loadInternally(request, context);
//        clearStaleReferences(instancePOs, context);
        return instancePOs;
    }

    protected abstract List<InstancePO> loadInternally(StoreLoadRequest request, IInstanceContext context);

    public void clearStaleReferences(List<InstancePO> instancePOs, IInstanceContext context) {
        Set<Long> refIds = new HashSet<>();
        for (InstancePO instancePO : instancePOs) {
            Type type = context.getType(instancePO.getTypeId());
            if(type instanceof ClassType classType) {
                extractRefIdsFromObject(instancePO, classType, refIds);
            }
            else if(type instanceof ArrayType arrayType) {
                extractRefIdsFromArray((InstanceArrayPO) instancePO, arrayType, refIds);
            }
        }
        Set<Long> aliveRefIds = getAliveInstanceIds(context.getTenantId(), refIds);
        for (InstancePO instancePO : instancePOs) {
            Type type = context.getType(instancePO.getTypeId());
            if(type instanceof ClassType classType) {
                clearStaleRefIdsForObject(instancePO, classType, aliveRefIds);
            }
            else if(type instanceof ArrayType arrayType) {
                clearStaleRefIdsForArray((InstanceArrayPO) instancePO, arrayType, aliveRefIds);
            }
        }
    }

    private void extractRefIdsFromObject(InstancePO instancePO, ClassType type, Set<Long> refIds) {
        for (Field field : type.getAllFields()) {
            boolean fieldIsRef = field.isReference();
            Object fieldValue = instancePO.get(field.getDeclaringType().getIdRequired(), field.getColumnName());
            NncUtils.invokeIfNotNull(convertToRefId(fieldValue, fieldIsRef), refIds::add);
        }
    }

    private void extractRefIdsFromArray(InstanceArrayPO arrayPO, ArrayType arrayType, Set<Long> refIds) {
        boolean elementIsRef = arrayType.getElementType().isReference();
        List<Object> elements = arrayPO.getElements();
        for (Object element : elements) {
            NncUtils.invokeIfNotNull(convertToRefId(element, elementIsRef), refIds::add);
        }
    }

    private void clearStaleRefIdsForObject(InstancePO instancePO, ClassType type, Set<Long> aliveRefIds) {
        for (Field field : type.getAllFields()) {
            boolean fieldIsRef = field.isReference();
            long typeId = field.getDeclaringType().getIdRequired();
            Object fieldValue = instancePO.get(typeId, field.getColumnName());
            Long refId = convertToRefId(fieldValue, fieldIsRef);
            if(refId != null && !aliveRefIds.contains(refId)) {
                instancePO.set(typeId, field.getColumnName(), null);
            }
        }
    }

    private void clearStaleRefIdsForArray(InstanceArrayPO arrayPO, ArrayType arrayType, Set<Long> aliveRefIds) {
        boolean elementIsRef = arrayType.getElementType().isReference();
        List<Object> elements = arrayPO.getElements();
        List<Object> newElements = new ArrayList<>();
        for (Object element : elements) {
            Long refId = convertToRefId(element, elementIsRef);
            if(refId != null && aliveRefIds.contains(refId)) {
                newElements.add(element);
            }
        }
        arrayPO.setElements(newElements);
    }

    private @Nullable Long convertToRefId(@Nullable Object fieldValue, boolean isRef) {
        if(isRef && (fieldValue instanceof Long refId)) {
            return refId;
        }
        if(fieldValue instanceof IdentityPO identityPO) {
            return identityPO.id();
        }
        return null;
    }

}
