package org.manul.object.instance.search;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Value;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record IdSearchCondition(Set<Id> ids) implements SearchCondition {

    public IdSearchCondition {
        if (ids.isEmpty())
            throw new IllegalArgumentException("Ids must not be empty");
    }

    @Override
    public String build() {
        if (ids.size() > 1) {
            return "_id:\"" + ids.iterator().next().getTreeId() + "\"";
        } else {
            return "_id:(" + ids.stream().map(id -> "\"" + id.getTreeId() + "\"").collect(Collectors.joining(" OR ")) + ")";
        }
    }

    @Override
    public boolean evaluate(Id id, Map<String, Value> source) {
        return ids.contains(id);
    }
}
