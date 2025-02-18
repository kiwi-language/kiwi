package org.metavm.object.instance;

import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceFactory {

    public static final Map<Class<? extends Value>, Method> ALLOCATE_METHOD_MAP = new ConcurrentHashMap<>();
    public static final String ALLOCATE_METHOD_NAME = "allocate";

    public static <T extends Instance> T allocate(Class<T> instanceType, boolean ephemeral, boolean isNew) {
        return allocate(instanceType, null, ephemeral, isNew);
    }

    public static <T extends Instance> T allocate(Class<T> instanceType, Id id, boolean ephemeral, boolean isNew) {
        T instance;
        if (instanceType == ArrayInstance.class)
            instance = instanceType.cast(new ArrayInstance(id, Types.getAnyArrayType(), ephemeral));
        else
            instance = instanceType.cast(new MvClassInstance(id, ClassInstance.uninitializedKlass.getType(), ephemeral, isNew));
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
                                 @Nullable ClassInstance parent,
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
            return create(instanceDTO, parent, context);
        }
    }

    public static Reference create(
            InstanceDTO instanceDTO,
            @Nullable ClassInstance parent,
            IInstanceContext context) {
        Utils.require(instanceDTO.isNew(),
                "Id of new instance must be null or zero");
        Type type = TypeParser.parseType(instanceDTO.type(), context.getTypeDefProvider()) ;
        Instance instance;
        var param = instanceDTO.param();
        if (param instanceof ClassInstanceParam classInstanceParam) {
            var classType = (ClassType) type;
            Map<String, InstanceFieldDTO> fieldMap = Utils.toMap(classInstanceParam.fields(), InstanceFieldDTO::fieldId);
            var id = parent != null ? parent.nextChildId() : context.allocateRootId();
            ClassInstance object = ClassInstance.allocate(id, classType, parent);
            instance = object;
            classType.forEachField(field -> {
                var tag = field.getFieldId().toString();
                if (fieldMap.containsKey(tag)) {
                    var fieldValue = resolveValue(
                            fieldMap.get(tag).value(),
                            field.getPropertyType(),
                            object,
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
            ArrayInstance array = new ArrayInstance(arrayType);
            instance = array;
            var elements = Utils.map(
                    arrayInstanceParam.elements(),
                    v -> resolveValue(v, arrayType.getElementType(), parent, context)
            );
            array.addAll(elements);
        } else if (param instanceof ListInstanceParam listInstanceParam) {
            var listType = (ClassType) type;
            var values = Utils.map(
                    listInstanceParam.elements(),
                    v -> resolveValue(v, listType.getTypeArguments().getFirst(), null, context)
            );
            instance = Instances.newList(listType, values);
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
                                     @Nullable ClassInstance parent,
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
            return save(instanceFieldValue.getInstance(), parent, context);
        } else if (rawValue instanceof ArrayFieldValue arrayFieldValue) {
            if (arrayFieldValue.getId() != null) {
                ArrayInstance arrayInstance = (ArrayInstance) context.get(Id.parse(arrayFieldValue.getId()));
                arrayInstance.clear();
                arrayInstance.setElements(
                        Utils.map(
                                arrayFieldValue.getElements(),
                                e -> resolveValue(e, arrayInstance.getInstanceType().getElementType(),
                                        parent,
                                        context)
                        )
                );
                return arrayInstance.getReference();
            } else {
                var array = ArrayInstance.allocate((ArrayType) type);
                var elements = Utils.map(
                        arrayFieldValue.getElements(),
                        e -> resolveValue(e, Types.getAnyType(),
                                parent, context)
                );
                array.reset(elements);
                return array.getReference();
            }
        } else if (rawValue instanceof ListFieldValue listFieldValue) {
            if (listFieldValue.getId() != null) {
                var list = (ClassInstance) context.get(Id.parse(listFieldValue.getId()));
                var listNative = Instances.getListNative(list);
                listNative.clear();
                Utils.forEach(
                        listFieldValue.getElements(),
                        e -> listNative.add(resolveValue(e, list.getInstanceType().getFirstTypeArgument(), parent, context))
                );
                return list.getReference();
            } else {
                var classType = (ClassType) type;
                if(!classType.isList())
                    throw new InternalException(classType.getTypeDesc() + " is not a list type");
                ClassType klass;
                if(StdKlass.list.get().isType(classType.getTemplateType())) {
                    klass = KlassType.create(StdKlass.arrayList.get(), List.of(classType.getFirstTypeArgument()));
                }
                else
                    klass = classType;
                var elementType = klass.getTypeArguments().getFirst();
                var values = Utils.map(
                        listFieldValue.getElements(),
                        e -> resolveValue(e, elementType, parent, context)
                );
                var list = Instances.newList(klass, values);
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
