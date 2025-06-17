package org.metavm.object.instance.search;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.EntityQueryOp;
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
        var first = source.get(field);
        if (Objects.equals(value, first)) return true;
        else if (first instanceof StringReference s1 && value instanceof StringReference s2) {
            return SearchUtil.match(s1.getValue(), s2.getValue());
        } else if (first.getValueType().isArray()) {
            var array = first.resolveArray();
            return array.contains(value);
        }
        else return false;

    }
}
