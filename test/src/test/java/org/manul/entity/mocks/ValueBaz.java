package org.manul.entity.mocks;

import org.manul.api.Generated;
import org.manul.api.ValueObject;
import org.manul.wire.Wire;
import org.manul.object.instance.core.Reference;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.function.Consumer;

@Wire
public record ValueBaz(int value, Reference reference) implements ValueObject {

    @Generated
    public static ValueBaz read(MvInput input) {
        return new ValueBaz(input.readInt(), (Reference) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitInt();
        visitor.visitValue();
    }

    public void forEachReference(Consumer<Reference> action) {
        action.accept(reference);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("value", this.value());
        map.put("reference", this.reference().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.writeInt(value);
        output.writeValue(reference);
    }

    public java.util.Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
