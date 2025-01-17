package org.metavm.object.instance.search;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Map;

@Slf4j
public record OrSearchCondition(List<SearchCondition> items) implements SearchCondition {

    @Override
    public String build() {
        return "(" + String.join(" OR ", Utils.map(items, SearchCondition::build)) + ")";
    }

    @Override
    public boolean evaluate(Map<String, Value> source) {
        return Utils.anyMatch(items, item -> item.evaluate(source));
    }
}
