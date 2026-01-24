package org.manul.object.instance.search;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.StringReference;
import org.manul.object.instance.core.Value;
import org.manul.util.Instances;

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
    public boolean evaluate(Id id, Map<String, Value> source) {
        return SearchUtil.prefixMatch(Instances.toJavaString(source.get(field)), Instances.toJavaString(value));
    }

}
