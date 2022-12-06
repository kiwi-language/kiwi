package tech.metavm.object.instance.search;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

import static tech.metavm.constant.FieldNames.TENANT_ID;
import static tech.metavm.constant.FieldNames.TYPE_ID;

public class IndexSourceBuilder {

    public static Map<String, Object> buildSource(long tenantId, Instance instance) {
        Type type = instance.getType();
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

    private static void setEsValue(Object value, Field field, Map<String, Object> source) {
        Type type = field.getType();
        if (type.isNullable()) {
            if(value == null) {
                source.put(field.getColumnName(), null);
                return;
            }
            type = type.getUnderlyingType();
        }
        NncUtils.requireNonNull(field.getColumn(), "Column required for field '" + field + "'");
        NncUtils.requireNonNull(value, "Getting null value for non null field  '" + field + "'");
        if(type.isArray() || type.isValue()) {
            return;
        }
        if(type.isReference()) {
            source.put(field.getColumnName(), ((Instance) value).getId());
        }
        else {
            if(value instanceof Instance instance) {
                throw new InternalException("Unexpected instance value: " +  instance + " for field '" + field + "'");
            }
            source.put(field.getColumnName(), value);
            if(field.getColumn().type() == SQLColumnType.VARCHAR64) {
                source.put(field.getColumn().fuzzyName(), value);
            }
        }
    }


}
