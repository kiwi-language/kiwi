package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceOutput;

import java.util.List;

public record TaggedClassTypeKey(Id id, int tag) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.TAGGED_CLASS);
        output.writeId(id);
        output.writeInt(tag);
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
