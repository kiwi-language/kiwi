package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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

    public static Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return create(instanceDTO, context::getType, context);
    }

    public static Instance create(
            InstanceDTO instanceDTO,
            Function<Long, Type> getType,
            IInstanceContext context
    ) {
        Type type = getType.apply(instanceDTO.typeRef().id());

        if(type instanceof ClassType classType) {
            ClassInstanceParam param = (ClassInstanceParam) instanceDTO.param();
            Map<Long, InstanceFieldDTO> fieldMap = NncUtils.toMap(param.fields(), InstanceFieldDTO::fieldId);
            Map<Field, Instance> data = new HashMap<>();
            for (Field field : classType.getAllFields()) {
                if (fieldMap.containsKey(field.getId())) {
                    data.put(
                            field,
                            resolveValue(
                                    fieldMap.get(field.getId()).value(),
                                    field.getType(),
                                    getType,
                                    context
                            )
                    );
                }
            }

            Instance instance = new ClassInstance(
                    data,
                    classType
            );
            if(!instance.getType().isEphemeral()) {
                context.bind(instance);
            }
            return instance;
        }
        else if(type instanceof ArrayType arrayType){
            ArrayParamDTO param = (ArrayParamDTO) instanceDTO.param();
            ArrayInstance arrayInstance = new ArrayInstance(
                    arrayType,
                    NncUtils.map(
                            param.elements(),
                            v -> resolveValue(v, arrayType.getElementType(), getType, context)
                    )
            );
            context.bind(arrayInstance);
            return arrayInstance;
        }
        else {
            throw new InternalException("Can not create instance for type '" + type + "'");
        }
    }

    public static Instance resolveValue(FieldValue rawValue, Type type, IEntityContext context) {
        return resolveValue(rawValue, type, context::getType,
                Objects.requireNonNull(context.getInstanceContext()));
    }

    public static Instance resolveValue(FieldValue rawValue, Type type,
                                        Function<Long, Type> getType,
                                        IInstanceContext context) {
        if(rawValue == null) {
            return InstanceUtils.nullInstance();
        }
        if(type.isNullable()) {
            type = type.getUnderlyingType();
        }
        if(rawValue instanceof PrimitiveFieldValue primitiveFieldValue) {
            if(type.isPassword()) {
                return new PasswordInstance(
                        EncodingUtils.md5((String) primitiveFieldValue.getValue()),
                        StandardTypes.getPasswordType()
                );
            }
            return InstanceUtils.resolvePrimitiveValue(type, primitiveFieldValue.getValue());
        }
        else if(rawValue instanceof ReferenceFieldValueDTO referenceFieldValue){
            return context.get(referenceFieldValue.getId());
        }
        else if(rawValue instanceof InstanceFieldValueDTO instanceFieldValue) {
            return create(instanceFieldValue.getInstance(), getType, context);
        }
        else if(rawValue instanceof ArrayFieldValueDTO arrayFieldValue) {
            if(arrayFieldValue.getId() != null && arrayFieldValue.getId() != 0) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(arrayFieldValue.getId());
                arrayInstance.clear();
                arrayInstance.setElements(
                        NncUtils.map(
                                arrayFieldValue.getElements(),
                                e -> resolveValue(e, arrayInstance.getType().getElementType(), getType, context)
                        )
                );
                return arrayInstance;
            }
            else {
                var elements = NncUtils.map(
                        arrayFieldValue.getElements(),
                        e -> resolveValue(e, StandardTypes.getObjectType(), getType, context)
                );
//                var effectiveArrayType = context.getEntityContext().getArrayType(
//                        ValueUtil.getCommonSuperType(NncUtils.map(elements, Instance::getType)),
//                        arrayFieldValue.isElementAsChild() ? ArrayKind.CHILD : ArrayKind.READ_WRITE
//                );
                return new ArrayInstance((ArrayType) type, elements);
            }
        }
        throw new InternalException("Can not resolve field value: " + rawValue);
    }

}
