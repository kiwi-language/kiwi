package tech.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;

import java.util.List;

public record ClassTypeKey(@NotNull String id) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.CLASS);
        output.writeId(Id.parse(id));
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
    public void acceptChildren(TypeKeyVisitor<?> visitor) {

    }

    @Override
    public int getCode() {
        return TypeKeyCodes.CLASS;
    }
}
