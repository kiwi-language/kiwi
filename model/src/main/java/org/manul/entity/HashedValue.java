package org.manul.entity;

import org.manul.api.Entity;
import org.manul.api.Generated;
import org.manul.api.ValueObject;
import org.manul.object.instance.core.Reference;
import org.manul.util.EncodingUtils;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;
import org.manul.wire.*;

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
