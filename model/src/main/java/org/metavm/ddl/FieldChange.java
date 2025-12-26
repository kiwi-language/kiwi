package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Wire
@Entity
public record FieldChange(
        String klassId,
        String fieldId,
        int oldTag,
        int newTag,
        FieldChangeKind kind
) implements ValueObject {

    @Generated
    public static FieldChange read(MvInput input) {
        return new FieldChange(input.readUTF(), input.readUTF(), input.readInt(), input.readInt(), FieldChangeKind.fromCode(input.read()));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitUTF();
        visitor.visitInt();
        visitor.visitInt();
        visitor.visitByte();
    }

    public void forEachReference(Consumer<Reference> action) {
    }

    @Generated
    public void write(MvOutput output) {
        output.writeUTF(klassId);
        output.writeUTF(fieldId);
        output.writeInt(oldTag);
        output.writeInt(newTag);
        output.write(kind.code());
    }

}
