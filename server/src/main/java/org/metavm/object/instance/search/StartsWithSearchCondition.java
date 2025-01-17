package org.metavm.object.instance.search;

import org.metavm.object.instance.core.StringValue;
import org.metavm.object.instance.core.Value;

import java.util.Map;
import java.util.Objects;

public record StartsWithSearchCondition(
        String field,
        StringValue value
) implements SearchCondition {

    @Override
    public String build() {
        return field + ":" + SearchBuilder.toString(value.toSearchConditionValue()) + "*";
    }

    @Override
    public boolean evaluate(Map<String, Value> source) {
        return source.get(field) instanceof StringValue str && str.value.startsWith(value.value);
    }

}
