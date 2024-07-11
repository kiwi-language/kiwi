package org.metavm.object.instance.search;

import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.instance.core.PrimitiveInstance;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metavm.constant.FieldNames.*;

public class IndexSourceBuilder {

    public static Map<String, Object> buildSource(long appId, ClassInstance instance) {
        Klass type = instance.getKlass();
        Map<String, Object> source = new HashMap<>();
        source.put(APPLICATION_ID, appId);
        source.put(TYPE, type.getType().toExpression());
        source.put(ID, instance.getStringId());

        List<Klass> hierarchy = type.getAncestorClasses();
        for (int lev = 0; lev < hierarchy.size(); lev++) {
            Map<String, Object> subSource = new HashMap<>();
            source.put("l" + lev, subSource);
            for (Field field : hierarchy.get(lev).getReadyFields()) {
                setEsValue(
                        instance.getField(field),
                        field,
                        subSource
                );
            }
        }
        return source;
    }

    private static void setEsValue(Instance value, Field field, Map<String, Object> source) {
        if (!field.getColumn().searchable()) {
            return;
        }
        Object esValue = getEsValue(value);
        source.put(field.getColumnName(), esValue);
        if (field.getColumn().kind() == ColumnKind.STRING) {
            source.put(field.getColumn().fuzzyName(), esValue);
        }
    }

    private static Object getEsValue(Instance value) {
        if (value.isArray()) {
            return NncUtils.map(value.resolveArray().getElements(), IndexSourceBuilder::getEsValue);
        } else if (value instanceof PrimitiveInstance primitiveInstance) {
            return primitiveInstance.getValue();
        } else {
            return ((InstanceReference) value).tryGetTreeId();
        }
    }

}
