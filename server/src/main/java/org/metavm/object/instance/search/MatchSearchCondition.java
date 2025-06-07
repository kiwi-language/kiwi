package org.metavm.object.instance.search;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.EntityQueryOp;
import org.metavm.object.instance.core.Value;

import java.util.Map;

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
        return EntityQueryOp.EQ.evaluate(srcValue, value);
    }
}
