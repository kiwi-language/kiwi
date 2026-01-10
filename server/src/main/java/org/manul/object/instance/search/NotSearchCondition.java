package org.manul.object.instance.search;

import org.manul.object.instance.core.Value;

import java.util.Map;

public record NotSearchCondition(SearchCondition operand) implements SearchCondition {
    @Override
    public String build() {
        return "NOT " + operand.build();
    }

    @Override
    public boolean evaluate(Map<String, Value> source) {
        return !operand.evaluate(source);
    }
}
