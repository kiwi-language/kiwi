package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.ArrayParamDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.ObjectParamDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
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
        Method allocateMethod = getAllocateMethod(instanceType, type.getClass());
        T instance = instanceType.cast(ReflectUtils.invoke(null, allocateMethod, type));
        if(id != null) {
            instance.initId(id);
        }
        return instance;
    }

    private static Method getAllocateMethod(Class<? extends Instance> instanceType,
                                            Class<? extends Type> typeType) {
        return ALLOCATE_METHOD_MAP.computeIfAbsent(
                instanceType,
                t -> ReflectUtils.getMethod(instanceType, ALLOCATE_METHOD_NAME, typeType)
        );
    }

    public static Instance create(InstanceDTO instanceDTO, InstanceContext context) {
        return create(instanceDTO, context.getEntityContext()::getType, context::get, context::bind);
    }

    public static Instance create(
            InstanceDTO instanceDTO,
            Function<Long, Type> getType,
            Function<Long, Instance> getInstance,
            Consumer<Instance> bindInstance
    ) {
        Type type = getType.apply(instanceDTO.typeId());

        if(type instanceof ClassType classType) {
            ObjectParamDTO param = (ObjectParamDTO) instanceDTO.param();
            Map<Long, InstanceFieldDTO> fieldMap = NncUtils.toMap(param.fields(), InstanceFieldDTO::fieldId);
            Map<Field, Instance> data = new HashMap<>();
            for (Field field : classType.getFields()) {
                if (fieldMap.containsKey(field.getId())) {
                    data.put(
                            field,
                            resolveValue(
                                    fieldMap.get(field.getId()).value(),
                                    field.getType(),
                                    getInstance
                            )
                    );
                }
            }

            Instance instance = new ClassInstance(
                    data,
                    classType
            );
            bindInstance.accept(instance);
            return instance;
        }
        else if(type instanceof ArrayType arrayType){
            ArrayParamDTO param = (ArrayParamDTO) instanceDTO.param();
            return new ArrayInstance(
                    arrayType,
                    NncUtils.map(
                            param.elements(),
                            v -> resolveValue(v, arrayType.getElementType(), getInstance)
                    )
            );
        }
        else {
            throw new InternalException("Can not create instance for type '" + type + "'");
        }
    }

    private static Instance resolveValue(Object rawValue, Type type, Function<Long, Instance> getInstance) {
        if(rawValue == null) {
            return null;
        }
        if(type.isPrimitive()) {
            return InstanceUtils.resolvePrimitiveValue(type, rawValue);
        }
        else {
            return getInstance.apply((long) rawValue);
        }
    }

}
