package org.metavm.object.instance.search;

import org.metavm.object.instance.core.Value;

import java.util.Map;

public interface SearchCondition {

    String build();

    boolean evaluate(Map<String, Value> source);

}
