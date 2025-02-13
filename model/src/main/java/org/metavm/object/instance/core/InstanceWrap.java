package org.metavm.object.instance.core;

import java.util.List;
import java.util.Map;

public class InstanceWrap {

    public static InstanceWrap from(Object value) {
        var inst = convertValue(value);
        if(inst instanceof InstanceWrap instanceWrap)
            return instanceWrap;
        else
            throw new IllegalArgumentException("Invalid instance value: " + value);
    }

    public static Object convertValue(Object value) {
        if(value instanceof Map<?,?> m)
            //noinspection unchecked
            return new ClassInstanceWrap((Map<String, Object>) m);
        else if(value instanceof List<?> l)
            //noinspection unchecked
            return new ArrayInstanceWrap((List<Object>) l);
        else
            return value;

    }

}
