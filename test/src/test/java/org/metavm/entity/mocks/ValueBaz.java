package org.metavm.entity.mocks;

import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

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
