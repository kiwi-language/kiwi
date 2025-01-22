package org.metavm.object.type;

import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

public record RemovedField(
        long klassTag,
        int fieldTag,
        String qualifiedName
) implements ValueObject {

    @Generated
    public static RemovedField read(MvInput input) {
        return new RemovedField(input.readLong(), input.readInt(), input.readUTF());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitInt();
        visitor.visitUTF();
    }

    public static RemovedField fromField(Field oldField) {
        return new RemovedField(oldField.getKlassTag(), oldField.getTag(), oldField.getQualifiedName());
    }

    public void forEachReference(Consumer<Reference> action) {
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("klassTag", this.klassTag());
        map.put("fieldTag", this.fieldTag());
        map.put("qualifiedName", this.qualifiedName());
    }

    @Generated
    public void write(MvOutput output) {
        output.writeLong(klassTag);
        output.writeInt(fieldTag);
        output.writeUTF(qualifiedName);
    }

    public java.util.Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
