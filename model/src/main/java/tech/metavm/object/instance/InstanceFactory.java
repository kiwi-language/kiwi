package tech.metavm.object.instance;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.ListNative;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InstanceFactory {

    public static final Map<Class<? extends Instance>, Method> ALLOCATE_METHOD_MAP = new ConcurrentHashMap<>();
    public static final String ALLOCATE_METHOD_NAME = "allocate";

    public static <T extends Instance> T allocate(Class<T> instanceType, Type type, boolean ephemeral) {
        return allocate(instanceType, type, null, ephemeral);
    }

    public static <T extends Instance> T allocate(Class<T> instanceType, Type type, Id id, boolean ephemeral) {
        T instance;
        if (type instanceof ArrayType arrayType)
            instance = instanceType.cast(new ArrayInstance(id, arrayType, ephemeral, null));
        else
            instance = instanceType.cast(new ClassInstance(id, (ClassType) type, ephemeral, null));
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

    public static Instance create(InstanceDTO instanceDTO, IInstanceContext context, ParameterizedTypeProvider parameterizedTypeProvider) {
        return create(instanceDTO, context::getType, null, context, parameterizedTypeProvider);
    }

    public static Instance save(InstanceDTO instanceDTO,
                                Function<Id, Type> getType,
                                @Nullable InstanceParentRef parentRef,
                                IInstanceContext context, ParameterizedTypeProvider parameterizedTypeProvider) {
        if (!instanceDTO.isNew()) {
            var instance = context.get(instanceDTO.parseId());
            if (parentRef != null) {
                NncUtils.requireTrue(
                        Objects.equals(instance.getParentRef(), parentRef),
                        "Trying to change parent. instance id: " + instanceDTO.id());
            }
            return ValueFormatter.parseInstance(instanceDTO, context, parameterizedTypeProvider);
        } else {
            return create(instanceDTO, getType, parentRef, context, parameterizedTypeProvider);
        }
    }

    public static Instance create(
            InstanceDTO instanceDTO,
            Function<Id, Type> getType,
            @Nullable InstanceParentRef parentRef,
            IInstanceContext context,
            ParameterizedTypeProvider parameterizedTypeProvider) {
        NncUtils.requireTrue(instanceDTO.isNew(),
                "Id of new instance must be null or zero");
        Type type = getType.apply(Id.parse(instanceDTO.typeId()));
        DurableInstance instance;
        var param = instanceDTO.param();
        if (param instanceof ClassInstanceParam classInstanceParam) {
            var classType = (ClassType) type;
            Map<String, InstanceFieldDTO> fieldMap = NncUtils.toMap(classInstanceParam.fields(), InstanceFieldDTO::fieldId);
            ClassInstance object = ClassInstance.allocate(classType, parentRef);
            instance = object;
            for (Field field : classType.getAllFields()) {
                if (fieldMap.containsKey(field.getStringId())) {
                    var fieldValue = resolveValue(
                            fieldMap.get(field.getStringId()).value(),
                            field.getType(),
                            getType,
                            InstanceParentRef.ofObject(object, field),
                            context,
                            parameterizedTypeProvider
                    );
                    object.initField(field, fieldValue);
                } else {
                    object.initField(field, Instances.nullInstance());
                }
            }
            object.ensureAllFieldsInitialized();
        } else if (param instanceof ArrayInstanceParam arrayInstanceParam) {
            var arrayType = (ArrayType) type;
            ArrayInstance array = new ArrayInstance(arrayType, parentRef);
            instance = array;
            var elements = NncUtils.map(
                    arrayInstanceParam.elements(),
                    v -> resolveValue(v, arrayType.getElementType(), getType,
                            InstanceParentRef.ofArray(array), context, parameterizedTypeProvider)
            );
            array.addAll(elements);
        } else if (param instanceof ListInstanceParam listInstanceParam) {
            var listType = (ClassType) type;
            var list = ClassInstance.allocate(listType);
            var listNative = new ListNative(list);
            listNative.List();
            NncUtils.forEach(
                    listInstanceParam.elements(),
                    v -> listNative.add(resolveValue(v, listType.getTypeArguments().get(0), getType, null, context, parameterizedTypeProvider))
            );
            instance = list;
        } else {
            throw new InternalException("Can not create instance for type '" + type + "'");
        }
        if (instanceDTO.sourceMappingId() != null) {
            var sourceMapping = context.getMappingProvider().getMapping(Id.parse(instanceDTO.sourceMappingId()));
            var source = sourceMapping.unmap(instance, context, context.getParameterizedFlowProvider());
            instance.setSourceRef(new SourceRef(source, sourceMapping));
            context.bind(instance);
            context.bind(source);
        } else
            context.bind(instance);
        return instance;
    }

    public static Instance resolveValue(FieldValue rawValue, Type type, IEntityContext context) {
        return resolveValue(rawValue, type, context::getType, null,
                Objects.requireNonNull(context.getInstanceContext()), context.getGenericContext());
    }

    public static Instance resolveValue(FieldValue rawValue, Type type,
                                        Function<Id, Type> getType,
                                        @Nullable InstanceParentRef parentRef,
                                        IInstanceContext context, ParameterizedTypeProvider parameterizedTypeProvider) {
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
            return save(instanceFieldValue.getInstance(), getType, parentRef, context, parameterizedTypeProvider);
        } else if (rawValue instanceof ArrayFieldValue arrayFieldValue) {
            if (arrayFieldValue.getId() != null) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(Id.parse(arrayFieldValue.getId()));
                arrayInstance.clear();
                arrayInstance.setElements(
                        NncUtils.map(
                                arrayFieldValue.getElements(),
                                e -> resolveValue(e, arrayInstance.getType().getElementType(), getType,
                                        InstanceParentRef.ofArray(arrayInstance),
                                        context, parameterizedTypeProvider)
                        )
                );
                return arrayInstance;
            } else {
                var array = ArrayInstance.allocate((ArrayType) type);
                var elements = NncUtils.map(
                        arrayFieldValue.getElements(),
                        e -> resolveValue(e, StandardTypes.getAnyType(), getType,
                                InstanceParentRef.ofArray(array), context, parameterizedTypeProvider)
                );
                array.setParentInternal(parentRef);
                array.reset(elements);
                return array;
            }
        } else if (rawValue instanceof ListFieldValue listFieldValue) {
            if (listFieldValue.getId() != null) {
                var list = (ClassInstance) context.get(Id.parse(listFieldValue.getId()));
                var listNative = new ListNative(list);
                listNative.clear();
                NncUtils.forEach(
                        listFieldValue.getElements(),
                        e -> listNative.add(resolveValue(e, list.getType().getListElementType(), getType, null, context, parameterizedTypeProvider))
                );
                return list;
            } else {
                var classType = (ClassType) type;
                NncUtils.requireTrue(classType.isList());
                if(classType.getEffectiveTemplate() == StandardTypes.getListType()) {
                    if(listFieldValue.isElementAsChild())
                        classType = parameterizedTypeProvider.getParameterizedType(StandardTypes.getChildListType(), List.of(classType.getListElementType()));
                    else
                        classType = parameterizedTypeProvider.getParameterizedType(StandardTypes.getReadWriteListType(), List.of(classType.getListElementType()));
                }
                var list = ClassInstance.allocate(classType);
                var listNative = new ListNative(list);
                listNative.List();
                NncUtils.forEach(
                        listFieldValue.getElements(),
                        e -> listNative.add(resolveValue(e, list.getType().getListElementType(), getType, null, context, parameterizedTypeProvider))
                );
                list.setParentInternal(parentRef);
                return list;
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
