package org.manul.object.instance.search;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Value;

import java.util.Map;

public interface SearchCondition {

    String build();

    boolean evaluate(Id id, Map<String, Value> source);

}
