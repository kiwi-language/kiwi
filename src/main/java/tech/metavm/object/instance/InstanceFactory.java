package tech.metavm.object.instance;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PasswordInstance;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
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
        if (id != null) {
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
        return create(instanceDTO, context::getType, null, context);
    }

    public static Instance save(InstanceDTO instanceDTO,
                                Function<Long, Type> getType,
                                @Nullable InstanceParentRef parentRef,
                                IInstanceContext context) {
        if (instanceDTO.id() != null && instanceDTO.id() != 0L) {
            var instance = context.get(RefDTO.fromId(instanceDTO.id()));
            NncUtils.requireTrue(
                    Objects.equals(instance.getParentRef(), parentRef),
                    "Trying to change parent. instance id: " + instanceDTO.id());
            return ValueFormatter.parseInstance(instanceDTO, context);
        } else {
            return create(instanceDTO, getType, parentRef, context);
        }
    }

    public static Instance create(
            InstanceDTO instanceDTO,
            Function<Long, Type> getType,
            @Nullable InstanceParentRef parentRef,
            IInstanceContext context
    ) {
        NncUtils.requireTrue(instanceDTO.id() == null || instanceDTO.id() == 0L,
                "Id of new instance must be null or zero");
        Type type = getType.apply(instanceDTO.typeRef().id());
        if (type instanceof ClassType classType) {
            ClassInstanceParam param = (ClassInstanceParam) instanceDTO.param();
            Map<Long, InstanceFieldDTO> fieldMap = NncUtils.toMap(param.fields(), InstanceFieldDTO::fieldId);
            ClassInstance instance = new ClassInstance(classType, parentRef);
            for (Field field : classType.getAllFields()) {
                if (fieldMap.containsKey(field.getId())) {
                    var fieldValue = resolveValue(
                            fieldMap.get(field.getId()).value(),
                            field.getType(),
                            getType,
                            InstanceParentRef.ofObject(instance, field),
                            context
                    );
                    if (!field.isChildField())
                        instance.initField(field, fieldValue);
                } else {
                    if (!field.isChildField()) {
                        instance.initField(field, InstanceUtils.nullInstance());
                    }
                }
            }
            instance.ensureAllFieldsInitialized();
            if (!instance.getType().isEphemeral()) {
                context.bind(instance);
            }
            return instance;
        } else if (type instanceof ArrayType arrayType) {
            ArrayParamDTO param = (ArrayParamDTO) instanceDTO.param();
            ArrayInstance array = new ArrayInstance(arrayType, parentRef);
            var elements = NncUtils.map(
                    param.elements(),
                    v -> resolveValue(v, arrayType.getElementType(), getType,
                            InstanceParentRef.ofArray(array), context)
            );
            if (!array.isChildArray()) {
                array.addAll(elements);
            }
            context.bind(array);
            return array;
        } else {
            throw new InternalException("Can not create instance for type '" + type + "'");
        }
    }

    public static Instance resolveValue(FieldValue rawValue, Type type, IEntityContext context) {
        return resolveValue(rawValue, type, context::getType,  null,
                Objects.requireNonNull(context.getInstanceContext()));
    }

    public static Instance resolveValue(FieldValue rawValue, Type type,
                                        Function<Long, Type> getType,
                                        @Nullable InstanceParentRef parentRef,
                                        IInstanceContext context) {
        if (rawValue == null) {
            return InstanceUtils.nullInstance();
        }
        if (type.isNullable()) {
            type = type.getUnderlyingType();
        }
        if (rawValue instanceof PrimitiveFieldValue primitiveFieldValue) {
            if (type.isPassword()) {
                return new PasswordInstance(
                        EncodingUtils.md5((String) primitiveFieldValue.getValue()),
                        StandardTypes.getPasswordType()
                );
            }
            return InstanceUtils.resolvePrimitiveValue(type, primitiveFieldValue.getValue());
        } else if (rawValue instanceof ReferenceFieldValue referenceFieldValue) {
            return context.get(referenceFieldValue.getId());
        } else if (rawValue instanceof InstanceFieldValue instanceFieldValue) {
            return save(instanceFieldValue.getInstance(), getType, parentRef, context);
        } else if (rawValue instanceof ArrayFieldValue arrayFieldValue) {
            if (arrayFieldValue.getId() != null && arrayFieldValue.getId() != 0) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(arrayFieldValue.getId());
                arrayInstance.clear();
                arrayInstance.setElements(
                        NncUtils.map(
                                arrayFieldValue.getElements(),
                                e -> resolveValue(e, arrayInstance.getType().getElementType(), getType,
                                        InstanceParentRef.ofArray(arrayInstance),
                                        context)
                        )
                );
                return arrayInstance;
            } else {
                var array = ArrayInstance.allocate((ArrayType) type);
                var elements = NncUtils.map(
                        arrayFieldValue.getElements(),
                        e -> resolveValue(e, StandardTypes.getObjectType(), getType,
                                InstanceParentRef.ofArray(array), context)
                );
                array.reloadParent(parentRef);
                array.reload(elements);
                return array;
            }
        }
        throw new InternalException("Can not resolve field value: " + rawValue);
    }

}
