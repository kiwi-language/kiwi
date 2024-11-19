package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.List;

public record ClassTypeKey(@NotNull Id id) implements TypeKey, GenericDeclarationRefKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.CLASS_TYPE);
        output.writeId(id);
    }

    @Override
    public String toTypeExpression() {
        return String.format("$$%s", id);
    }

    @Override
    public ClassType toType(TypeDefProvider typeDefProvider) {
        return new ClassType(typeDefProvider.getKlass(id), List.of());
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitClassTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.CLASS_TYPE;
    }

    @Override
    public ClassType resolve(TypeDefProvider typeDefProvider) {
        return toType(typeDefProvider);
    }
}
