package org.metavm.object.type;

import org.metavm.entity.natives.ListNative;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ValueFormatter {

    private static final Logger logger = LoggerFactory.getLogger(ValueFormatter.class);

    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static DurableInstance parseInstance(InstanceDTO instanceDTO, IInstanceContext context) {
        Type actualType;
        if (!instanceDTO.isNew())
            actualType = context.get(instanceDTO.parseId()).getType();
        else
            actualType = TypeParser.parseType(instanceDTO.type(), context.getTypeDefProvider());
        if (actualType instanceof ClassType classType) {
            if (classType.isList()) {
                var param = (ListInstanceParam) instanceDTO.param();
                ClassInstance list;
                ListNative listNative;
                if (!instanceDTO.isNew()) {
                    list = (ClassInstance) context.get(instanceDTO.parseId());
                    listNative = new ListNative(list);
                    listNative.clear();
                } else {
                    list = ClassInstance.allocate(classType);
                    listNative = new ListNative(list);
                    listNative.List();
                }
                for (FieldValue element : param.elements()) {
                    listNative.add(
                            parseOne(element, classType.getFirstTypeArgument(),
                                    null, context));
                }
                return list;
            } else {
                Map<Field, Instance> fieldValueMap = new HashMap<>();
                ClassInstanceParam param = (ClassInstanceParam) instanceDTO.param();
                Map<String, InstanceFieldDTO> fieldDTOMap = NncUtils.toMap(
                        param.fields(),
                        InstanceFieldDTO::fieldId
                );
                ClassInstance instance;
                var klass = classType.resolve();
                if (!instanceDTO.isNew()) {
                    instance = (ClassInstance) context.get(instanceDTO.parseId());
                } else {
                    instance = ClassInstance.allocate(classType);
                }
                for (Field field : klass.getAllFields()) {
                    FieldValue rawValue = NncUtils.get(fieldDTOMap.get(field.getStringTag()), InstanceFieldDTO::value);
                    Instance fieldValue = rawValue != null ?
                            parseOne(rawValue, field.getType(), InstanceParentRef.ofObject(instance, field), context)
                            : Instances.nullInstance();
                    fieldValueMap.put(field, fieldValue);
                }
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
                return instance;
            }
        } else if (actualType instanceof ArrayType arrayType) {
            ArrayInstanceParam param = (ArrayInstanceParam) instanceDTO.param();
            ArrayInstance array;
            if (!instanceDTO.isNew()) {
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
            array.setElements(elements);
            return array;
        } else {
            throw new InternalException("Can not parse instance of type '" + actualType + "'");
        }
    }

    private static Instance parseOne(FieldValue rawValue, Type type,
                                     @Nullable InstanceParentRef parentRef, IInstanceContext context) {
        Instance value = InstanceFactory.resolveValue(
                rawValue, type, parentRef, context
        );
        if (value instanceof DurableInstance d && d.tryGetId() == null && !context.containsInstance(d))
            context.bind(d);
        return value;
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
