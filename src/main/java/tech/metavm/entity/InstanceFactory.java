package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceRelation;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class InstanceFactory {

    public static final Map<Class<? extends Instance>, Method> ALLOCATE_METHOD_MAP = new ConcurrentHashMap<>();
    public static final String ALLOCATE_METHOD_NAME = "allocate";

    public static <T extends Instance> T allocate(Class<T> instanceType, Type type) {
        return allocate(instanceType, type, null);
    }

    public static <T extends Instance> T allocate(Class<T> instanceType, Type type, Long id) {
        Method allocateMethod = getAllocateMethod(instanceType);
        T instance = instanceType.cast(ReflectUtils.invoke(null, allocateMethod, type));
        if(id != null) {
            instance.initId(id);
        }
        return instance;
    }

    private static Method getAllocateMethod(Class<? extends Instance> instanceType) {
        return ALLOCATE_METHOD_MAP.computeIfAbsent(
                instanceType,
                t -> ReflectUtils.getMethod(instanceType, ALLOCATE_METHOD_NAME, Type.class)
        );
    }

    public static Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return create(instanceDTO, context.getEntityContext()::getType, context::get, context::bind);
    }

    public static Instance create(
            InstanceDTO instanceDTO,
            Function<Long, Type> getType,
            Function<Long, IInstance> getInstance,
            Consumer<Instance> bindInstance
    ) {
        Type type = getType.apply(instanceDTO.typeId());

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
                type
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
                instancePO.getSyncVersion()
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
                context.getEntityContext().getField(relationPO.getFieldId()),
                context.get(relationPO.getDestInstanceId())
        );
    }

}
