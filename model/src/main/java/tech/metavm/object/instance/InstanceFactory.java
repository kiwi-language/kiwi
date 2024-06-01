package tech.metavm.object.instance;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.ListNative;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.object.view.ObjectMappingRef;
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

public class InstanceFactory {

    public static final Map<Class<? extends Instance>, Method> ALLOCATE_METHOD_MAP = new ConcurrentHashMap<>();
    public static final String ALLOCATE_METHOD_NAME = "allocate";

    public static <T extends Instance> T allocate(Class<T> instanceType, boolean ephemeral) {
        return allocate(instanceType, null, ephemeral);
    }

    public static <T extends Instance> T allocate(Class<T> instanceType, Id id, boolean ephemeral) {
        T instance;
        if (instanceType == ArrayInstance.class)
            instance = instanceType.cast(new ArrayInstance(id, StandardTypes.getAnyArrayType(), ephemeral, null));
        else
            instance = instanceType.cast(new ClassInstance(id, ClassInstance.uninitializedKlass.getType(), ephemeral, null));
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

    public static DurableInstance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return create(instanceDTO, null, context);
    }

    public static DurableInstance save(InstanceDTO instanceDTO,
                                @Nullable InstanceParentRef parentRef,
                                IInstanceContext context) {
        if (!instanceDTO.isNew()) {
            var instance = context.get(instanceDTO.parseId());
            if (parentRef != null) {
                NncUtils.requireTrue(
                        Objects.equals(instance.getParentRef(), parentRef),
                        "Trying to change parent. instance id: " + instanceDTO.id());
            }
            return ValueFormatter.parseInstance(instanceDTO, context);
        } else {
            return create(instanceDTO, parentRef, context);
        }
    }

    public static DurableInstance create(
            InstanceDTO instanceDTO,
            @Nullable InstanceParentRef parentRef,
            IInstanceContext context) {
        NncUtils.requireTrue(instanceDTO.isNew(),
                "Id of new instance must be null or zero");
        Type type = TypeParser.parseType(instanceDTO.type(), context.getTypeDefProvider()) ;
        DurableInstance instance;
        var param = instanceDTO.param();
        if (param instanceof ClassInstanceParam classInstanceParam) {
            var classType = (ClassType) type;
            Map<String, InstanceFieldDTO> fieldMap = NncUtils.toMap(classInstanceParam.fields(), InstanceFieldDTO::fieldId);
            ClassInstance object = ClassInstance.allocate(classType, parentRef);
            instance = object;
            for (Field field : classType.resolve().getAllFields()) {
                var tag = field.getTag().toString();
                if (fieldMap.containsKey(tag)) {
                    var fieldValue = resolveValue(
                            fieldMap.get(tag).value(),
                            field.getType(),
                            InstanceParentRef.ofObject(object, field),
                            context
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
                    v -> resolveValue(v, arrayType.getElementType(),
                            InstanceParentRef.ofArray(array), context)
            );
            array.addAll(elements);
        } else if (param instanceof ListInstanceParam listInstanceParam) {
            var listType = (ClassType) type;
            var list = ClassInstance.allocate(listType);
            var listNative = new ListNative(list);
            listNative.List();
            NncUtils.forEach(
                    listInstanceParam.elements(),
                    v -> listNative.add(resolveValue(v, listType.getTypeArguments().get(0), null, context
                    ))
            );
            instance = list;
        } else {
            throw new InternalException("Can not create instance for type '" + type + "'");
        }
        if (instanceDTO.sourceMappingRef() != null) {
            var mappingRefDTO = instanceDTO.sourceMappingRef();
            var sourceMapping = new ObjectMappingRef(
                    (ClassType) TypeParser.parseType(mappingRefDTO.declaringType(), context.getTypeDefProvider()),
                    context.getMappingProvider().getObjectMapping(Id.parse(mappingRefDTO.rawMappingId()))
            ).resolve();
            var source = sourceMapping.unmap(instance, context);
            instance.setSourceRef(new SourceRef(source, sourceMapping));
            context.bind(instance);
            if(!context.containsInstance(source))
                context.bind(source);
        } else
            context.bind(instance);
        return instance;
    }

    public static Instance resolveValue(FieldValue rawValue, Type type, IEntityContext context) {
        return resolveValue(rawValue, type, null,
                Objects.requireNonNull(context.getInstanceContext()));
    }

    public static Instance resolveValue(FieldValue rawValue, Type type,
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
            return save(instanceFieldValue.getInstance(), parentRef, context);
        } else if (rawValue instanceof ArrayFieldValue arrayFieldValue) {
            if (arrayFieldValue.getId() != null) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(Id.parse(arrayFieldValue.getId()));
                arrayInstance.clear();
                arrayInstance.setElements(
                        NncUtils.map(
                                arrayFieldValue.getElements(),
                                e -> resolveValue(e, arrayInstance.getType().getElementType(),
                                        InstanceParentRef.ofArray(arrayInstance),
                                        context)
                        )
                );
                return arrayInstance;
            } else {
                var array = ArrayInstance.allocate((ArrayType) type);
                var elements = NncUtils.map(
                        arrayFieldValue.getElements(),
                        e -> resolveValue(e, StandardTypes.getAnyType(),
                                InstanceParentRef.ofArray(array), context)
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
                        e -> listNative.add(resolveValue(e, list.getKlass().getListElementType(), null, context))
                );
                return list;
            } else {
                var classType = (ClassType) type;
                if(!classType.isList())
                    throw new InternalException(classType.getTypeDesc() + " is not a list type");
                Klass klass;
                if(StandardTypes.getListKlass().isType(classType.getEffectiveTemplate())) {
                    if(listFieldValue.isElementAsChild())
                        klass = StandardTypes.getChildListKlass().getParameterized(List.of(classType.getListElementType()));
                    else
                        klass = StandardTypes.getReadWriteListKlass().getParameterized(List.of(classType.getListElementType()));
                }
                else
                    klass = classType.resolve();
                var list = ClassInstance.allocate(klass.getType());
                var listNative = new ListNative(list);
                listNative.List();
                NncUtils.forEach(
                        listFieldValue.getElements(),
                        e -> listNative.add(resolveValue(e, list.getType().getListElementType(), null, context))
                );
                list.setParentInternal(parentRef);
                return list;
            }
        }
        throw new InternalException("Can not resolve field value: " + rawValue);
    }

    private static PrimitiveInstance resolvePrimitiveValue(PrimitiveFieldValue fieldValue) {
        var kind = PrimitiveKind.fromCode(fieldValue.getPrimitiveKind());
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
