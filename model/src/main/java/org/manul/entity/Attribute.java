package org.manul.entity;

import lombok.extern.slf4j.Slf4j;
import org.manul.api.Entity;
import org.manul.api.Generated;
import org.manul.api.ValueObject;
import org.manul.object.instance.core.Reference;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;
import org.manul.wire.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Wire
@Slf4j
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

    @Generated
    public static Attribute read(MvInput input) {
        return new Attribute(input.readUTF(), input.readUTF());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitUTF();
    }

    public void forEachReference(Consumer<Reference> action) {
    }

    @Generated
    public void write(MvOutput output) {
        output.writeUTF(name);
        output.writeUTF(value);
    }

}
