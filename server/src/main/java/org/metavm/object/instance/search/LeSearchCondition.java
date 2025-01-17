package org.metavm.object.instance.search;

import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.instance.core.Value;

import java.util.Map;

public record LeSearchCondition(
        String field,
        NumberValue value
) implements SearchCondition {
    @Override
    public String build() {
        return field + ":<=" + SearchBuilder.toString(value.toSearchConditionValue());
    }
    @Override
    public boolean evaluate(Map<String, Value> source) {
        var srcValue = source.get(field);
        return srcValue instanceof NumberValue numberValue && numberValue.le(value).value;
    }

}
