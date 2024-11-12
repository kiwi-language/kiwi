package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.LocalKey;
import org.metavm.flow.NodeRT;
import org.metavm.flow.ScopeRT;
import org.metavm.object.type.Type;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public abstract class NestedMapping extends Entity implements LocalKey {

    public abstract Type generateMappingCode(Supplier<NodeRT> getSource, ScopeRT scope);

    public abstract Type generateUnmappingCode(Supplier<NodeRT> viewSupplier, ScopeRT scope);

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
