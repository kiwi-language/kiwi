package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.Constants;
import org.metavm.util.InstanceOutput;

import java.util.List;

public record TaggedClassTypeKey(Id id, int tag) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.TAGGED_CLASS);
        output.writeId(id);
        output.writeLong(tag);
    }

    @Override
    public String toTypeExpression() {
        return Constants.ID_PREFIX + id + ":" + tag;
    }

    @Override
    public Type toType(TypeDefProvider typeDefProvider) {
        return new ClassType(typeDefProvider.getKlass(id), List.of());
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitTaggedClassTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.TAGGED_CLASS;
    }

    @Override
    @JsonIgnore
    public int getTypeTag() {
        return tag;
    }
}
