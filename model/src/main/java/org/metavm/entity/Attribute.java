package org.metavm.entity;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public record Attribute(
        String name,
        String value
) implements ValueObject {

    public static List<Attribute> fromMap(Map<String, String> map) {
        var attributes = new ArrayList<Attribute>();
        map.forEach((name, value) -> attributes.add(new Attribute(name, value)));
        return attributes;
    }

    public void write(MvOutput output) {
        output.writeUTF(name);
        output.writeUTF(value);
    }

    public static Attribute read(MvInput input) {
        return new Attribute(input.readUTF(), input.readUTF());
    }

}
