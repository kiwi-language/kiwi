package org.metavm.autograph;

import java.util.HashMap;
import java.util.Map;

public class StateHolder {

    private final Map<String, Object> values = new HashMap<>();

    public String getString(String varName) {
        return (String) values.get(varName);
    }

    public void setString(String varName, String value) {
        values.put(varName, value);
    }



}
