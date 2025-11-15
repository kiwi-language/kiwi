package org.metavm.entity;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.EncodingUtils;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.wire.*;

import java.util.function.Consumer;

@Wire
@Entity
public record HashedValue(
        String salt,
        String hashedValue
) implements ValueObject {

    @Generated
    public static HashedValue read(MvInput input) {
        return new HashedValue(input.readUTF(), input.readUTF());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitUTF();
    }

    public boolean verify(String value) {
        return EncodingUtils.verifySecureHash(value, salt, hashedValue);
    }

    public void forEachReference(Consumer<Reference> action) {
    }

    @Generated
    public void write(MvOutput output) {
        output.writeUTF(salt);
        output.writeUTF(hashedValue);
    }

}
