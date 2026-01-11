package org.manul.object.instance.search;

import org.manul.object.instance.core.Value;

import java.util.Map;

public interface SearchCondition {

    String build();

    boolean evaluate(Map<String, Value> source);

}
