package tech.metavm.object.meta;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

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
        Type actualType = context.getType(instanceDTO.typeId());
        if(actualType instanceof ClassType classType) {
            Map<Field, Instance> fieldValueMap = new HashMap<>();
            ClassInstanceParamDTO param = (ClassInstanceParamDTO) instanceDTO.param();
            Map<Long, InstanceFieldDTO> fieldDTOMap = NncUtils.toMap(
                    param.fields(),
                    InstanceFieldDTO::fieldId
            );
            for (Field field : classType.getFields()) {
                FieldValueDTO rawValue = NncUtils.get(fieldDTOMap.get(field.getId()), InstanceFieldDTO::value);
                Instance fieldValue = rawValue != null ?
                        parseOne(rawValue, field.getType(), context) : InstanceUtils.nullInstance();
                fieldValueMap.put(field, fieldValue);
            }
            ClassInstance instance;
            if (instanceDTO.id() != null) {
                instance = (ClassInstance) context.get(instanceDTO.id());
                fieldValueMap.forEach(instance::set);
            } else {
                instance = new ClassInstance(fieldValueMap, classType);
                context.bind(instance);
            }
            return instance;
        }
        else if(actualType instanceof ArrayType arrayType){
            ArrayParamDTO param = (ArrayParamDTO) instanceDTO.param();
            List<Instance> elements = new ArrayList<>();
            for (FieldValueDTO element : param.elements()) {
                elements.add(
                        parseOne(element, arrayType.getElementType(), context)
                );
            }
            ArrayInstance array;
            if(instanceDTO.id() != null) {
                array = (ArrayInstance) context.get(instanceDTO.id());
                array.setElements(elements);
            }
            else {
                array = new ArrayInstance(arrayType, elements);
            }
            return array;
        }
        else {
            throw new InternalException("Can not parse instance of type '" + actualType + "'");
        }
    }

    public static Instance parseReference(ReferenceFieldValueDTO referenceDTO, IInstanceContext context) {
        return context.get(referenceDTO.getId());
    }

    private static Instance parseOne(FieldValueDTO rawValue, Type type, IInstanceContext context) {
        Instance value =  InstanceFactory.resolveValue(
                rawValue, type, context::getType, context::get
        );
        if(value.isReference() && value.getId() == null) {
            context.bind(value);
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
//        throw new InternalException("Can not parse field value '" + rawValue + "'");
    }

    public static Object format(Instance value) {
        if(value == null) {
            return null;
        }
        if(value instanceof PrimitiveInstance primitiveInstance) {
            if(primitiveInstance.getType().isPassword()) {
                return null;
            }
            else {
                return primitiveInstance.getValue();
            }
        }
        else {
            if(value.getType().isValue()) {
                return value.toDTO();
            }
            else if(value.getId() != null){
                return new ReferenceDTO(value.getId());
            }
            else {
                return null;
            }
        }
    }

    public static String formatDate(Long time) {
        if(time == null) {
            return null;
        }
        return DATE_FORMAT.format(new Date(time));
    }

    public static String formatTime(Long time) {
        if(time == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(new Date(time));
    }

    public static String formatDouble(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public static Long parseDate(String source) {
        if(source == null) {
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
