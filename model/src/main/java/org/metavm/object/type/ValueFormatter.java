package org.metavm.object.type;

import org.metavm.entity.natives.ArrayListNative;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ValueFormatter {

    private static final Logger logger = LoggerFactory.getLogger(ValueFormatter.class);

    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Reference parseInstance(InstanceDTO instanceDTO, IInstanceContext context) {
        Type actualType;
        if (!instanceDTO.isNew())
            actualType = context.get(instanceDTO.parseId()).getInstanceType();
        else
            actualType = TypeParser.parseType(instanceDTO.type(), context.getTypeDefProvider());
        if (actualType instanceof KlassType classType) {
            if (classType.isList()) {
                var param = (ListInstanceParam) instanceDTO.param();
                MvClassInstance list;
                ArrayListNative listNative;
                var values = param.elements().stream().map(
                        element -> parseOne(element, classType.getFirstTypeArgument(), null, context)
                );
                if (!instanceDTO.isNew()) {
                    list = (MvClassInstance) context.get(instanceDTO.parseId());
                    listNative = Instances.getListNative(list);
                    listNative.clear();
                    values.forEach(listNative::add);
                } else {
                    list = Instances.newList(classType, values.toList());
                }
                return list.getReference();
            } else {
                Map<Field, Value> fieldValueMap = new HashMap<>();
                ClassInstanceParam param = (ClassInstanceParam) instanceDTO.param();
                Map<String, InstanceFieldDTO> fieldDTOMap = Utils.toMap(
                        param.fields(),
                        InstanceFieldDTO::fieldId
                );
                ClassInstance instance;
                if (!instanceDTO.isNew()) {
                    instance = (ClassInstance) context.get(instanceDTO.parseId());
                } else {
                    instance = ClassInstance.allocate(context.allocateRootId(), classType);
                }
                classType.foreachField(field -> {
                    FieldValue rawValue = Utils.safeCall(fieldDTOMap.get(field.getRawField().getStringId()), InstanceFieldDTO::value);
                    Value fieldValue = rawValue != null ?
                            parseOne(rawValue, field.getPropertyType(), instance, context)
                            : Instances.nullInstance();
                    fieldValueMap.put(field.getRawField(), fieldValue);
                });
                if (!instanceDTO.isNew()) {
                    fieldValueMap.forEach((field, value) -> {
                        if (!field.isReadonly())
                            instance.setField(field, value);
                    });
                } else {
                    fieldValueMap.forEach(instance::initField);
                    instance.ensureAllFieldsInitialized();
                    context.bind(instance);
                }
                return instance.getReference();
            }
        } else if (actualType instanceof ArrayType arrayType) {
            ArrayInstanceParam param = (ArrayInstanceParam) instanceDTO.param();
            ArrayInstance array;
            if (!instanceDTO.isNew()) {
                array = (ArrayInstance) context.get(instanceDTO.parseId());
            } else {
                array = new ArrayInstance(arrayType);
            }
            List<Value> elements = new ArrayList<>();
            for (FieldValue element : param.elements()) {
                elements.add(
                        parseOne(element, arrayType.getElementType(), null, context)
                );
            }
            array.setElements(elements);
            return array.getReference();
        } else {
            throw new InternalException("Can not parse instance of type '" + actualType + "'");
        }
    }

    private static Value parseOne(FieldValue rawValue, Type type,
                                  @Nullable ClassInstance parent, IInstanceContext context) {
        Value value = InstanceFactory.resolveValue(
                rawValue, type, parent, context
        );
        if (value instanceof Reference r && r.tryGetId() == null && !context.containsInstance(r.get()))
            context.bind(r.get());
        return value;
    }

    public static Object format(Value value) {
        if (value == null) {
            return null;
        }
        if (value instanceof NullValue)
            return null;
        if (value instanceof PrimitiveValue primitiveValue) {
            if (primitiveValue.getValueType().isPassword()) {
                return null;
            } else {
                return primitiveValue.getValue();
            }
        } else {
            var d = (Instance) value.resolveDurable();
            if (value.getValueType().isValueType()) {
                return value.toDTO();
            } else if (d.tryGetTreeId() != null) {
                return new ReferenceDTO(d.getTreeId());
            } else {
                return null;
            }
        }
    }

    public static String formatTime(Long time) {
        if (time == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(new Date(time));
    }

}
