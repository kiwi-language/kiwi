package tech.metavm.object.instance;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

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
        T instance;
        if(type instanceof ArrayType arrayType)
            instance =  instanceType.cast(new ArrayInstance(id, arrayType, null));
        else
            instance = instanceType.cast(new ClassInstance(id, (ClassType) type, null));
//        Method allocateMethod = getAllocateMethod(instanceType, type.getClass());
//        T instance = instanceType.cast(ReflectUtils.invoke(null, allocateMethod, type));
        return instance;
    }

    private static Method getAllocateMethod(Class<? extends Instance> instanceType,
                                            Class<? extends Type> typeType) {
        return ALLOCATE_METHOD_MAP.computeIfAbsent(
                instanceType,
                t -> ReflectionUtils.getMethod(instanceType, ALLOCATE_METHOD_NAME, typeType)
        );
    }

    public static Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return create(instanceDTO, context::getType, null, context);
    }

    public static Instance save(InstanceDTO instanceDTO,
                                Function<Long, Type> getType,
                                @Nullable InstanceParentRef parentRef,
                                IInstanceContext context) {
        if (instanceDTO.id() != null) {
            var instance = context.get(instanceDTO.parseId());
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
        NncUtils.requireTrue(instanceDTO.id() == null,
                "Id of new instance must be null or zero");
        Type type = getType.apply(instanceDTO.typeRef().id());
        if (type instanceof ClassType classType) {
            ClassInstanceParam param = (ClassInstanceParam) instanceDTO.param();
            Map<Long, InstanceFieldDTO> fieldMap = NncUtils.toMap(param.fields(), InstanceFieldDTO::fieldId);
            ClassInstance instance = ClassInstance.allocate(classType, parentRef);
            for (Field field : classType.getAllFields()) {
                if (fieldMap.containsKey(field.getId())) {
                    var fieldValue = resolveValue(
                            fieldMap.get(field.getId()).value(),
                            field.getType(),
                            getType,
                            InstanceParentRef.ofObject(instance, field),
                            context
                    );
                    if (!field.isChild())
                        instance.initField(field, fieldValue);
                } else {
                    if (!field.isChild()) {
                        instance.initField(field, Instances.nullInstance());
                    }
                }
            }
            instance.ensureAllFieldsInitialized();
            if (!instance.getType().isEphemeral()) {
                context.bind(instance);
            }
            return instance;
        } else if (type instanceof ArrayType arrayType) {
            ArrayInstanceParam param = (ArrayInstanceParam) instanceDTO.param();
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
            return Instances.nullInstance();
        }
        if (type.isBinaryNullable()) {
            type = type.getUnderlyingType();
        }
        if (rawValue instanceof PrimitiveFieldValue primitiveFieldValue) {
//            if (type.isPassword()) {
//                return new PasswordInstance(
//                        EncodingUtils.md5((String) primitiveFieldValue.getValue()),
//                        StandardTypes.getPasswordType()
//                );
//            }
            return resolvePrimitiveValue(primitiveFieldValue);
        } else if (rawValue instanceof ReferenceFieldValue referenceFieldValue) {
            return context.get(Id.parse(referenceFieldValue.getId()));
        } else if (rawValue instanceof InstanceFieldValue instanceFieldValue) {
            return save(instanceFieldValue.getInstance(), getType, parentRef, context);
        } else if (rawValue instanceof ArrayFieldValue arrayFieldValue) {
            if (arrayFieldValue.getId() != null) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(Id.parse(arrayFieldValue.getId()));
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
                        e -> resolveValue(e, StandardTypes.getAnyType(), getType,
                                InstanceParentRef.ofArray(array), context)
                );
                array.resetParent(parentRef);
                array.reset(elements);
                return array;
            }
        }
        throw new InternalException("Can not resolve field value: " + rawValue);
    }

    private static PrimitiveInstance resolvePrimitiveValue(PrimitiveFieldValue fieldValue) {
        var kind = PrimitiveKind.getByCode(fieldValue.getPrimitiveKind());
        var value = fieldValue.getValue();
        return switch (kind) {
            case LONG -> Instances.longInstance(((Number) value).longValue());
            case DOUBLE -> Instances.doubleInstance(((Number) value).doubleValue());
            case BOOLEAN -> Instances.booleanInstance((Boolean) value);
            case PASSWORD -> Instances.passwordInstance((String) value);
            case STRING -> Instances.stringInstance((String) value);
            case TIME -> Instances.timeInstance(((Number) value).longValue());
            case NULL -> Instances.nullInstance();
            case VOID -> throw new InternalException("Invalid primitive kind 'void'");
        };
    }

}
