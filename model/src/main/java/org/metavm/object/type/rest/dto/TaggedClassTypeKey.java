package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.Constants;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.List;

public record TaggedClassTypeKey(Id id, int tag) implements TypeKey, GenericDeclarationRefKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.TAGGED_CLASS_TYPE);
        output.writeId(id);
        output.writeLong(tag);
    }

    @Override
    public String toTypeExpression() {
        return Constants.ID_PREFIX + id + ":" + tag;
    }

    @Override
    public ClassType toType(TypeDefProvider typeDefProvider) {
        return new ClassType(null, typeDefProvider.getKlass(id), List.of());
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitTaggedClassTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.TAGGED_CLASS_TYPE;
    }

    @Override
    @JsonIgnore
    public int getTypeTag() {
        return tag;
    }

    @Override
    public GenericDeclarationRef resolve(TypeDefProvider typeDefProvider) {
        return toType(typeDefProvider);
    }
}
