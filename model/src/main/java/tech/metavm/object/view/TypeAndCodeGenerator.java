package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.LocalKey;
import tech.metavm.object.type.Type;

@EntityType("TypeAndCodeGenerator")
public class TypeAndCodeGenerator extends Entity implements LocalKey {
    private final Type type;
    private final NestedMapping nestedMapping;

    public TypeAndCodeGenerator(Type type, NestedMapping nestedMapping) {
        this.type = type;
        this.nestedMapping = nestedMapping;
    }

    public Type getType() {
        return type;
    }

    public NestedMapping getCodeGenerator() {
        return nestedMapping;
    }

    @Override
    public boolean isValidLocalKey() {
        return type.getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return type.getCode();
    }
}
