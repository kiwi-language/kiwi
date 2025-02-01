package org.metavm.object.instance.search;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.StringReference;
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
        else if (srcValue instanceof StringReference s1 && value instanceof StringReference s2) {
            return s1.getValue().contains(s2.getValue());
        } else if (srcValue.getValueType().isArray()) {
            var array = srcValue.resolveArray();
            return array.contains(value);
        }
        else return false;
    }
}
