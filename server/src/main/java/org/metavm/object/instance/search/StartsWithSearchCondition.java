package org.metavm.object.instance.search;

import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import java.util.Map;

public record StartsWithSearchCondition(
        String field,
        Value value
) implements SearchCondition {

    @Override
    public String build() {
        return field + ":" + SearchBuilder.toString(value.toSearchConditionValue()) + "*";
    }

    @Override
    public boolean evaluate(Map<String, Value> source) {
        return SearchUtil.prefixMatch(Instances.toJavaString(source.get(field)), Instances.toJavaString(value));
    }

}
