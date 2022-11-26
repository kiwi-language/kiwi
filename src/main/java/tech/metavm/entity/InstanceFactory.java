package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.object.instance.InstanceRelation;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class InstanceFactory {

    public static Instance create(InstancePO instancePO, InstanceContext context) {
        if(instancePO instanceof InstanceArrayPO instanceArrayPO) {
            return createArray(instanceArrayPO, context);
        }
        else {
            return createInstance(instancePO, context);
        }
    }

    private static InstanceArray createArray(InstanceArrayPO instanceArrayPO, InstanceContext context) {
        return new InstanceArray(
                instanceArrayPO.getId(),
                context.getType(instanceArrayPO.getTypeId()),
                instanceArrayPO.getVersion(),
                instanceArrayPO.getSyncVersion(),
                context.getEntityType(instanceArrayPO.getTypeId()),
                NncUtils.map(instanceArrayPO.getElementIds(), context::get),
                instanceArrayPO.isElementAsChild()
            );
    }

    public static Instance create(InstanceDTO instanceDTO, InstanceContext context) {
        return create(instanceDTO, context::getType, context::get, context::bind);
    }

    public static Instance create(
            InstanceDTO instanceDTO,
            Function<Long, Type> getType,
            Function<Long, IInstance> getInstance,
            Consumer<Instance> bindInstance
    ) {
        Type type = getType.apply(instanceDTO.typeId());
        Class<?> entityType = EntityTypeRegistry.getEntityType(type.getId());

        Map<Long, InstanceFieldDTO> fieldMap = NncUtils.toMap(instanceDTO.fields(), InstanceFieldDTO::fieldId);
        Map<Field, Object> data = new HashMap<>();
        for (Field field : type.getFields()) {
            if(fieldMap.containsKey(field.getId())) {
                data.put(
                        field,
                        resolveFieldValue(
                                fieldMap.get(field.getId()).value(),
                                field,
                                getInstance
                        )
                );
            }
        }

        Instance instance =  new Instance(
                data,
                type,
                entityType
        );
        bindInstance.accept(instance);
        return instance;
    }

    private static Instance createInstance(InstancePO instancePO, InstanceContext context) {
        Type type = context.getType(instancePO.getTypeId());
        Map<String, Object> data = instancePO.getData();
        Map<Field, Object> resolvedData = new HashMap<>();
        for (Field field : type.getFields()) {
            if(data.containsKey(field.getColumnName())) {
                resolvedData.put(
                        field,
                        resolveFieldValue(
                                data.get(field.getColumnName()),
                                field,
                                context::get
                        )
                );
            }
        }
        return new Instance(
                instancePO.getId(),
                resolvedData,
                type,
                instancePO.getVersion(),
                instancePO.getSyncVersion(),
                context.getEntityType(instancePO.getId())
        );
    }

    private static Object resolveFieldValue(Object rawValue, Field field, Function<Long, IInstance> getInstance) {
        if(rawValue == null) {
            return null;
        }
        if(field.isPrimitive()) {
            return rawValue;
        }
        else {
            return getInstance.apply((long) rawValue);
        }
    }

    public static InstanceRelation createRelation(RelationPO relationPO, InstanceContext context) {
        return new InstanceRelation(
                context.get(relationPO.getSrcInstanceId()),
                context.getField(relationPO.getFieldId()),
                context.get(relationPO.getDestInstanceId())
        );
    }

}
