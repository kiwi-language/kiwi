package org.metavm.entity;

import org.metavm.api.EntityType;
import org.metavm.api.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EntityType
public record Attribute(
        String name,
        String value
) implements Value {

    public static List<Attribute> fromMap(Map<String, String> map) {
        var attributes = new ArrayList<Attribute>();
        map.forEach((name, value) -> attributes.add(new Attribute(name, value)));
        return attributes;
    }

}
