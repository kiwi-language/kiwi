package tech.metavm.object.type;

import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ValueFormatter {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//    public static Instance parse(Object rawValue, Type type, IInstanceContext context) {
//        if(isNull(rawValue)) {
//            if(type.isNullable()) {
//                return null;
//            }
//            else {
//                throw new InternalException("Type " + type.getName() + " can not have null value");
//            }
//        }
//        if(type.isNullable()) {
//            type = type.getUnderlyingType();
//        }
//        if(rawValue instanceof InstanceArrayDTO arrayDTO) {
//            return parseArray(arrayDTO, context);
//        }
//        else {
//            return parseOne(rawValue, type, context);
//        }
//    }

//    public static ArrayInstance parseArray(InstanceDTO arrayDTO, IInstanceContext context) {
//        List<Instance> elements = new ArrayList<>();
//        ArrayParamDTO arrayParamDTO = (ArrayParamDTO) arrayDTO.param();
//        for (Object value : arrayParamDTO.elements()) {
//            elements.add(parseOne(value, StandardTypes.getObjectType(), context));
//        }
//        ArrayInstance array;
//        if(arrayDTO.id() !=  null) {
//            array = (ArrayInstance) context.get(arrayDTO.id());
//            array.setElements(elements);
//        }
//        else {
//            array = new ArrayInstance(StandardTypes.getArrayType(), elements);
//            context.bind(array);
//        }
//        return array;
//    }

//    public static ArrayInstance parseArray(InstanceArrayDTO arrayDTO, IInstanceContext context) {
//        List<Instance> elements = new ArrayList<>();
//        for (Object value : arrayDTO.elements()) {
//            elements.add(parseOne(value, StandardTypes.getObjectType(), context));
//        }
//        ArrayInstance array;
//        if(arrayDTO.id() !=  null) {
//            array = (ArrayInstance) context.get(arrayDTO.id());
//            array.setElements(elements);
//        }
//        else {
//            array = new ArrayInstance(StandardTypes.getArrayType(), elements);
//            context.bind(array);
//        }
//        return array;
//    }

    public static Instance parseInstance(InstanceDTO instanceDTO, IInstanceContext context) {
        Type actualType;
        if (instanceDTO.id() != null) {
            actualType = context.get(instanceDTO.parseId()).getType();
        } else {
            actualType = context.getTypeProvider().getType(instanceDTO.typeRef());
        }
        if (actualType instanceof ClassType classType) {
            Map<Field, Instance> fieldValueMap = new HashMap<>();
            ClassInstanceParam param = (ClassInstanceParam) instanceDTO.param();
            Map<Long, InstanceFieldDTO> fieldDTOMap = NncUtils.toMap(
                    param.fields(),
                    InstanceFieldDTO::fieldId
            );
            ClassInstance instance;
            if (instanceDTO.id() != null) {
                instance = (ClassInstance) context.get(instanceDTO.parseId());
            } else {
                instance = ClassInstance.allocate(classType);
            }
            for (Field field : classType.getAllFields()) {
                FieldValue rawValue = NncUtils.get(fieldDTOMap.get(field.tryGetId()), InstanceFieldDTO::value);
                Instance fieldValue = rawValue != null ?
                        parseOne(rawValue, field.getType(), InstanceParentRef.ofObject(instance, field), context)
                        : Instances.nullInstance();
//                if (!field.isChild()) {
                    fieldValueMap.put(field, fieldValue);
//                }
            }
            if (instanceDTO.id() != null) {
                fieldValueMap.forEach((field, value) -> {
                    if (!field.isReadonly())
                        instance.setField(field, value);
                });
            } else {
                fieldValueMap.forEach(instance::initField);
                instance.ensureAllFieldsInitialized();
                context.bind(instance);
            }
//            if (instanceDTO.id() != null) {
//                instance = (ClassInstance) context.get(instanceDTO.id());
//                fieldValueMap.forEach(instance::setField);
//            } else {
//                instance = new ClassInstance(fieldValueMap, classType);
//                context.bind(instance);
//            }
            return instance;
        } else if (actualType instanceof ArrayType arrayType) {
            ArrayInstanceParam param = (ArrayInstanceParam) instanceDTO.param();
            ArrayInstance array;
            if (instanceDTO.id() != null) {
                array = (ArrayInstance) context.get(instanceDTO.parseId());
            } else {
                array = new ArrayInstance(arrayType);
            }
            List<Instance> elements = new ArrayList<>();
            for (FieldValue element : param.elements()) {
                elements.add(
                        parseOne(element, arrayType.getElementType(),
                                InstanceParentRef.ofArray(array),
                                context)
                );
            }
//            if (array.isChildArray()) {
//                Set<Instance> elementSet = new IdentitySet<>(elements);
//                for (Instance element : new ArrayList<>(array.getElements())) {
//                    if (!elementSet.contains(element)) {
//                        array.removeElement(element);
//                    }
//                }
//            } else {
            array.setElements(elements);
//            }
            return array;
        } else {
            throw new InternalException("Can not parse instance of type '" + actualType + "'");
        }
    }

    public static Instance parseReference(ReferenceFieldValue referenceDTO, IInstanceContext context) {
        return context.get(Id.parse(referenceDTO.getId()));
    }

    private static Instance parseOne(FieldValue rawValue, Type type,
                                     @Nullable InstanceParentRef parentRef, IInstanceContext context) {
        Instance value = InstanceFactory.resolveValue(
                rawValue, type, context::getType, parentRef, context
        );
        if (value instanceof DurableInstance d && d.tryGetPhysicalId() == null) {
            context.bind(d);
        }
        return value;
//        if(type.isNullable()) {
//            type = type.getUnderlyingType();
//        }
//        if(rawValue instanceof ReferenceFieldValueDTO ref) {
//            return parseReference(ref, context);
//        }
//        if(rawValue instanceof InstanceFieldValueDTO instanceFieldValue) {
//            return parseInstance(instanceFieldValue.getInstance(), context);
//        }
//        if(rawValue instanceof PrimitiveFieldValueDTO primitiveFieldValue) {
//            if (isPassword(type)) {
//                return new PasswordInstance(
//                        EncodingUtils.md5((String) primitiveFieldValue.getValue()),
//                        StandardTypes.getPasswordType()
//                );
//            }
//            return InstanceUtils.resolvePrimitiveValue(type, primitiveFieldValue.getValue());
//        }
//        if(rawValue instanceof ArrayFieldValueDTO arrayFieldValue) {
//            ArrayType arrayType = (type instanceof ArrayType a) ? a :
//                    ModelDefRegistry.getType(Object.class).getArrayType();
//            ArrayInstance arrayInstance = new ArrayInstance(
//                    arrayType,
//                    NncUtils.map(
//                            arrayFieldValue.getElements(),
//                            e -> parse(e, arrayType.getElementType(), context)
//                    )
//            );
//            context.bind(arrayInstance);
//            return arrayInstance;
//        }
//        throw new InternalException("Can not parse indexItem value '" + rawValue + "'");
    }

    public static Object format(Instance value) {
        if (value == null) {
            return null;
        }
        if (value instanceof PrimitiveInstance primitiveInstance) {
            if (primitiveInstance.getType().isPassword()) {
                return null;
            } else {
                return primitiveInstance.getValue();
            }
        } else {
            var d = (DurableInstance) value;
            if (value.getType().isValue()) {
                return value.toDTO();
            } else if (d.tryGetPhysicalId() != null) {
                return new ReferenceDTO(d.getPhysicalId());
            } else {
                return null;
            }
        }
    }

    public static String formatDate(Long time) {
        if (time == null) {
            return null;
        }
        return DATE_FORMAT.format(new Date(time));
    }

    public static String formatTime(Long time) {
        if (time == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(new Date(time));
    }

    public static String formatDouble(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public static Long parseDate(String source) {
        if (source == null) {
            return null;
        }
        try {
            return DATE_TIME_FORMAT.parse(source).getTime();
        } catch (ParseException e) {
            throw new InternalException(
                    "fail to parse date string '" + source + "' with date format: " + DATE_TIME_FORMAT, e
            );
        }
    }

    private static boolean isNull(Object rawValue) {
        return rawValue == null
                || (rawValue instanceof String && ((String) rawValue).length() == 0);
    }

    private static BusinessException invalidValue(Object rawValue, Type type) {
        return BusinessException.invalidValue(type, rawValue);
    }

}
