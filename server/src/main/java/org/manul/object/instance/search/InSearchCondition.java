package org.manul.object.instance.search;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record InSearchCondition(
        String field,
        List<Value> values
) implements SearchCondition {

    @Override
    public String build() {
        var items = new ArrayList<String>();
        for (Value element : values) {
            items.add(field + ":" + SearchBuilder.toString(element.toSearchConditionValue()));
        }
        return "(" + String.join(" OR ", items) + ")";
    }

    @Override
    public boolean evaluate(Id id, Map<String, Value> source) {
        var value = source.get(field);
        return value != null && values.contains(value);
    }
}
