package org.metavm.object.instance.search;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.StringValue;
import org.metavm.object.instance.core.Value;

import java.util.Map;
import java.util.Objects;

@Slf4j
public record MatchSearchCondition(
        String field,
        Value value
) implements SearchCondition {

    @Override
    public String build() {
        return field + ":" + SearchBuilder.toString(value.toSearchConditionValue());
    }

    @Override
    public boolean evaluate(Map<String, Value> source) {
        var srcValue = source.get(field);
        if (Objects.equals(value, srcValue)) return true;
        else if (srcValue instanceof StringValue srcStr && value instanceof StringValue matchStr) {
            var splits = srcStr.getValue().split(" ");
            var match = matchStr.value;
            for (String split : splits) if (split.equals(match)) return true;
            return false;
        } else if (srcValue.getValueType().isArray()) {
            var array = srcValue.resolveArray();
            return array.contains(value);
        }
        else return false;
    }
}
