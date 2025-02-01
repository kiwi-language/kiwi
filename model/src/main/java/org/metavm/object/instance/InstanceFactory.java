package org.metavm.object.instance;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.ListNative;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceFactory {

    public static final Map<Class<? extends Value>, Method> ALLOCATE_METHOD_MAP = new ConcurrentHashMap<>();
    public static final String ALLOCATE_METHOD_NAME = "allocate";

    public static <T extends Instance> T allocate(Class<T> instanceType, boolean ephemeral) {
        return allocate(instanceType, null, ephemeral);
    }

    public static <T extends Instance> T allocate(Class<T> instanceType, Id id, boolean ephemeral) {
        T instance;
        if (instanceType == ArrayInstance.class)
            instance = instanceType.cast(new ArrayInstance(id, Types.getAnyArrayType(), ephemeral, null));
        else
            instance = instanceType.cast(new MvClassInstance(id, ClassInstance.uninitializedKlass.getType(), ephemeral, null));
//        Method allocateMethod = getAllocateMethod(instanceType, type.getClass());
//        T instance = instanceType.cast(ReflectUtils.invoke(null, allocateMethod, type));
        return instance;
    }

    private static Method getAllocateMethod(Class<? extends Value> instanceType,
                                            Class<? extends Type> typeType) {
        return ALLOCATE_METHOD_MAP.computeIfAbsent(
                instanceType,
                t -> ReflectionUtils.getMethod(instanceType, ALLOCATE_METHOD_NAME, typeType)
        );
    }

    public static Reference create(InstanceDTO instanceDTO, IInstanceContext context) {
        return create(instanceDTO, null, context);
    }

    public static Reference save(InstanceDTO instanceDTO,
                                 @Nullable InstanceParentRef parentRef,
                                 IInstanceContext context) {
        if (!instanceDTO.isNew()) {
            var instance = context.get(instanceDTO.parseId());
//            if (parentRef != null) {
//                NncUtils.requireTrue(
//                        Objects.equals(instance.getParentRef(), parentRef),
//                        "Trying to change parent. instance id: " + instanceDTO.id());
//            }
            return ValueFormatter.parseInstance(instanceDTO, context);
        } else {
            return create(instanceDTO, parentRef, context);
        }
    }

    public static Reference create(
            InstanceDTO instanceDTO,
            @Nullable InstanceParentRef parentRef,
            IInstanceContext context) {
        Utils.require(instanceDTO.isNew(),
                "Id of new instance must be null or zero");
        Type type = TypeParser.parseType(instanceDTO.type(), context.getTypeDefProvider()) ;
        Instance instance;
        var param = instanceDTO.param();
        if (param instanceof ClassInstanceParam classInstanceParam) {
            var classType = (ClassType) type;
            Map<String, InstanceFieldDTO> fieldMap = Utils.toMap(classInstanceParam.fields(), InstanceFieldDTO::fieldId);
            ClassInstance object = ClassInstance.allocate(classType, parentRef);
            instance = object;
            classType.forEachField(field -> {
                var tag = field.getFieldId().toString();
                if (fieldMap.containsKey(tag)) {
                    var fieldValue = resolveValue(
                            fieldMap.get(tag).value(),
                            field.getPropertyType(),
                            InstanceParentRef.ofObject(object.getReference(), field.getRawField()),
                            context
                    );
                    object.initField(field.getRawField(), fieldValue);
                } else {
                    object.initField(field.getRawField(), Instances.nullInstance());
                }
            });
            object.ensureAllFieldsInitialized();
        } else if (param instanceof ArrayInstanceParam arrayInstanceParam) {
            var arrayType = (ArrayType) type;
            ArrayInstance array = new ArrayInstance(arrayType, parentRef);
            instance = array;
            var elements = Utils.map(
                    arrayInstanceParam.elements(),
                    v -> resolveValue(v, arrayType.getElementType(),
                            InstanceParentRef.ofArray(array.getReference()), context)
            );
            array.addAll(elements);
        } else if (param instanceof ListInstanceParam listInstanceParam) {
            var listType = (ClassType) type;
            var list = ClassInstance.allocate(listType);
            var listNative = new ListNative(list);
            listNative.List();
            Utils.forEach(
                    listInstanceParam.elements(),
                    v -> listNative.add(resolveValue(v, listType.getTypeArguments().getFirst(), null, context
                    ))
            );
            instance = list;
        } else {
            throw new InternalException("Can not create instance for type '" + type + "'");
        }
        context.bind(instance);
        return instance.getReference();
    }

    public static Value resolveValue(FieldValue rawValue, Type type, IInstanceContext context) {
        return resolveValue(rawValue, type, null,
                Objects.requireNonNull(context));
    }

    public static Value resolveValue(FieldValue rawValue, Type type,
                                     @Nullable InstanceParentRef parentRef,
                                     IInstanceContext context) {
        if (rawValue == null) {
            return Instances.nullInstance();
        }
        if (type.isBinaryNullable()) {
            type = type.getUnderlyingType();
        }
        if (rawValue instanceof NullFieldValue)
            return Instances.nullInstance();
        if (rawValue instanceof PrimitiveFieldValue primitiveFieldValue) {
//            if (type.isPassword()) {
//                return new PasswordInstance(
//                        EncodingUtils.md5((String) primitiveFieldValue.getValue()),
//                        StandardTypes.getPasswordType()
//                );
//            }
            return resolvePrimitiveValue(primitiveFieldValue);
        } else if (rawValue instanceof ReferenceFieldValue referenceFieldValue) {
            return context.get(Id.parse(referenceFieldValue.getId())).getReference();
        } else if (rawValue instanceof InstanceFieldValue instanceFieldValue) {
            return save(instanceFieldValue.getInstance(), parentRef, context);
        } else if (rawValue instanceof ArrayFieldValue arrayFieldValue) {
            if (arrayFieldValue.getId() != null) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(Id.parse(arrayFieldValue.getId()));
                arrayInstance.clear();
                arrayInstance.setElements(
                        Utils.map(
                                arrayFieldValue.getElements(),
                                e -> resolveValue(e, arrayInstance.getInstanceType().getElementType(),
                                        InstanceParentRef.ofArray(arrayInstance.getReference()),
                                        context)
                        )
                );
                return arrayInstance.getReference();
            } else {
                var array = ArrayInstance.allocate((ArrayType) type);
                var elements = Utils.map(
                        arrayFieldValue.getElements(),
                        e -> resolveValue(e, Types.getAnyType(),
                                InstanceParentRef.ofArray(array.getReference()), context)
                );
                array.setParentInternal(parentRef);
                array.reset(elements);
                return array.getReference();
            }
        } else if (rawValue instanceof ListFieldValue listFieldValue) {
            if (listFieldValue.getId() != null) {
                var list = (ClassInstance) context.get(Id.parse(listFieldValue.getId()));
                var listNative = new ListNative(list);
                listNative.clear();
                Utils.forEach(
                        listFieldValue.getElements(),
                        e -> listNative.add(resolveValue(e, list.getInstanceType().getFirstTypeArgument(), null, context))
                );
                return list.getReference();
            } else {
                var classType = (ClassType) type;
                if(!classType.isList())
                    throw new InternalException(classType.getTypeDesc() + " is not a list type");
                ClassType klass;
                if(StdKlass.list.get().isType(classType.getTemplateType())) {
                    if(listFieldValue.isElementAsChild())
                        klass = KlassType.create(StdKlass.childList.get(), List.of(classType.getFirstTypeArgument()));
                    else
                        klass = KlassType.create(StdKlass.arrayList.get(), List.of(classType.getFirstTypeArgument()));
                }
                else
                    klass = classType;
                var list = ClassInstance.allocate(klass);
                var listNative = new ListNative(list);
                listNative.List();
                Utils.forEach(
                        listFieldValue.getElements(),
                        e -> listNative.add(resolveValue(e, list.getInstanceType().getFirstTypeArgument(), null, context))
                );
                list.setParentInternal(parentRef);
                return list.getReference();
            }
        }
        throw new InternalException("Can not resolve field value: " + rawValue);
    }

    private static PrimitiveValue resolvePrimitiveValue(PrimitiveFieldValue fieldValue) {
        var kind = PrimitiveKind.fromCode(fieldValue.getPrimitiveKind());
        var value = fieldValue.getValue();
        return switch (kind) {
            case LONG -> Instances.longInstance(((Number) value).longValue());
            case INT -> Instances.intInstance(((Number) value).intValue());
            case DOUBLE -> Instances.doubleInstance(((Number) value).doubleValue());
            case FLOAT -> Instances.floatInstance(((Number) value).floatValue());
            case BOOLEAN -> Instances.booleanInstance((Boolean) value);
            case PASSWORD -> Instances.passwordInstance((String) value);
            case TIME -> Instances.timeInstance(((Number) value).longValue());
            case CHAR -> Instances.charInstance((Character) value);
            case SHORT -> Instances.shortInstance((Short) value);
            case BYTE -> Instances.byteInstance((Byte) value);
            case VOID -> throw new InternalException("Invalid primitive kind 'void'");
        };
    }

}
