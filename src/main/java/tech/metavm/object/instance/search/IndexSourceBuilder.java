package tech.metavm.object.instance.search;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

import static tech.metavm.constant.FieldNames.*;

public class IndexSourceBuilder {

    public static Map<String, Object> buildSource(long tenantId, ClassInstance instance) {
        ClassType type = instance.getType();
        Map<String, Object> source = new HashMap<>();
        source.put(TENANT_ID, tenantId);
        source.put(TYPE_ID, type.getId());
        source.put(ID, instance.getId());
        for (Field field : type.getAllFields()) {
            setEsValue(
                    instance.getField(field),
                    field,
                    source
            );
        }
        return source;
    }

    private static void setEsValue(Instance value, Field field, Map<String, Object> source) {
        if(!field.getColumn().searchable()) {
            return;
        }
        Object esValue  = getEsValue(value);
        source.put(field.getColumnName(), esValue);
        if(field.getColumn().type() == SQLType.VARCHAR64) {
            source.put(field.getColumn().fuzzyName(), esValue);
        }
    }

    private static Object getEsValue(Instance value) {
        if(value instanceof ArrayInstance arrayInstance) {
            return NncUtils.map(arrayInstance.getElements(), IndexSourceBuilder::getEsValue);
        }
        else if(value instanceof PrimitiveInstance primitiveInstance) {
            return primitiveInstance.getValue();
        }
        else {
            return value.getId();
        }
    }

}
