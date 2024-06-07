package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.LocalKey;
import tech.metavm.flow.ScopeRT;
import tech.metavm.flow.Value;
import tech.metavm.object.type.Type;

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
