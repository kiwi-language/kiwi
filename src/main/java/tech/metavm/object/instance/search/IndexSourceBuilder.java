package tech.metavm.object.instance.search;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;

import java.util.HashMap;
import java.util.Map;

import static tech.metavm.constant.FieldNames.TENANT_ID;
import static tech.metavm.constant.FieldNames.TYPE_ID;

public class IndexSourceBuilder {

    public static Map<String, Object> buildSource(long tenantId, ClassInstance instance) {
        ClassType type = instance.getType();
        Map<String, Object> source = new HashMap<>();
        source.put(TENANT_ID, tenantId);
        source.put(TYPE_ID, type.getId());
        for (Field field : type.getFields()) {
            setEsValue(
                    instance.get(field),
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
        if (value instanceof PrimitiveInstance primitiveInstance) {
            Object primitiveValue = primitiveInstance.getValue();
            source.put(field.getColumnName(), primitiveValue);
            if(field.getColumn().type() == SQLType.VARCHAR64) {
                source.put(field.getColumn().fuzzyName(), primitiveValue);
            }
        }
        else {
            source.put(field.getColumnName(), value.getId());
        }
    }


}
