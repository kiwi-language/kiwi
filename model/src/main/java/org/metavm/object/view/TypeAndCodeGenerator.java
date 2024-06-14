package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.LocalKey;
import org.metavm.object.type.Type;

@EntityType
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
