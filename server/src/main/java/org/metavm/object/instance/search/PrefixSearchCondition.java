package org.metavm.object.instance.search;

import org.metavm.object.instance.core.StringReference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import java.util.Map;

public record PrefixSearchCondition(
        String field,
        StringReference value
) implements SearchCondition {

    @Override
    public String build() {
        var esValue = SearchBuilder.escape(value.getValue()) + "*";
        return field + ":" + esValue;
    }

    @Override
    public boolean evaluate(Map<String, Value> source) {
        return SearchUtil.prefixMatch(Instances.toJavaString(source.get(field)), Instances.toJavaString(value));
    }

}
