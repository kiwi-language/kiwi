package org.manul.object.instance.search;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Value;
import org.manul.util.Utils;

import java.util.List;
import java.util.Map;

public record AndSearchCondition(
        List<SearchCondition> items
) implements SearchCondition {
    @Override
    public String build() {
        return String.join(" AND ", Utils.map(items, SearchCondition::build));
    }

    @Override
    public boolean evaluate(Id id, Map<String, Value> source) {
        return Utils.allMatch(items, item -> item.evaluate(id, source));
    }
}
