package org.metavm.object.instance.search;

import org.metavm.object.instance.core.Value;
import org.metavm.util.Utils;

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
    public boolean evaluate(Map<String, Value> source) {
        return Utils.allMatch(items, item -> item.evaluate(source));
    }
}
