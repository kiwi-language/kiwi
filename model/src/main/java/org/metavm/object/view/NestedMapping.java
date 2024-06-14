package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.LocalKey;
import org.metavm.flow.ScopeRT;
import org.metavm.flow.Value;
import org.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public abstract class NestedMapping extends Entity implements LocalKey {

    public abstract Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope);

    public abstract Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope);

    public abstract Type getTargetType();

    @Override
    public boolean isValidLocalKey() {
        return getTargetType().getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(getTargetType().getCode());
    }

    public abstract String getText();

}
