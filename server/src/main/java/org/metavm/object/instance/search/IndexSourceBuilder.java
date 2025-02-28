package org.metavm.object.instance.search;

import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.Utils;

import java.util.HashMap;
import java.util.Map;

import static org.metavm.constant.FieldNames.*;

public class IndexSourceBuilder {

    public static Map<String, Object> buildSource(long appId, ClassInstance instance) {
        Klass type = instance.getInstanceKlass();
        Map<String, Object> source = new HashMap<>();
        source.put(APPLICATION_ID, appId);
        source.put(TYPE, type.getType().toExpression());
        source.put(ID, instance.getStringId());
        var fields = instance.buildSource();
        fields.forEach((f, v) -> {
            Object esValue = getEsValue(v);
            source.put(f, esValue);
        });
//        List<Klass> hierarchy = type.getAncestorClasses();
//        for (int lev = 0; lev < hierarchy.size(); lev++) {
//            Map<String, Object> subSource = new HashMap<>();
//            source.put("l" + lev, subSource);
//            for (Field field : hierarchy.get(lev).getReadyFields()) {
//                setEsValue(
//                        instance.getField(field),
//                        field,
//                        subSource
//                );
//            }
//        }
        return source;
    }

    private static void setEsValue(Value value, Field field, Map<String, Object> source) {
        if (!field.getColumn().searchable()) {
            return;
        }
        Object esValue = getEsValue(value);
        source.put(field.getColumnName(), esValue);
        if (field.getColumn().kind() == ColumnKind.STRING) {
            source.put(field.getColumn().fuzzyName(), esValue);
        }
    }

    private static Object getEsValue(Value value) {
        if (value.isArray()) {
            return Utils.map(value.resolveArray().getElements(), IndexSourceBuilder::getEsValue);
        } else if (value instanceof NullValue)
            return null;
        else if (value instanceof PrimitiveValue primitiveValue) {
            return primitiveValue.getValue();
        } else if (value instanceof StringReference stringReference)
            return stringReference.getValue();
        else {
            return ((EntityReference) value).tryGetTreeId();
        }
    }

}
