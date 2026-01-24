package org.manul.object.instance.search;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.NumberValue;
import org.manul.object.instance.core.Value;

import java.util.Map;

public record GeSearchCondition(
        String field,
        NumberValue value
) implements SearchCondition {

    @Override
    public String build() {
        return field + ":>=" + SearchBuilder.toString(value.toSearchConditionValue());
    }

    @Override
    public boolean evaluate(Id id, Map<String, Value> source) {
        var srcValue = source.get(field);
        return srcValue instanceof NumberValue numberValue && numberValue.ge(value).value;
    }

}
