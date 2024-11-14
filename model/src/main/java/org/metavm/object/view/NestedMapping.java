package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.LocalKey;
import org.metavm.flow.Node;
import org.metavm.flow.Code;
import org.metavm.object.type.Type;

import java.util.function.Supplier;

@EntityType
public abstract class NestedMapping extends Entity implements LocalKey {

    public abstract Type generateMappingCode(Supplier<Node> getSource, Code code);

    public abstract Type generateUnmappingCode(Supplier<Node> viewSupplier, Code code);

    public abstract Type getTargetType();

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getTargetType().getName();
    }

    public abstract String getText();

}
