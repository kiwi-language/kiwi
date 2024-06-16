package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.TypeDTO;

import javax.annotation.Nullable;

@EntityType
public abstract class ChildTypeNode extends NodeRT {

    @ChildEntity
    private final Klass klass;

    protected ChildTypeNode(Long tmpId, String name, @Nullable String code, @NotNull Klass klass, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope);
        this.klass = addChild(klass, "klass");
    }

    public Klass getKlass() {
        return klass;
    }

    @NotNull
    @Override
    public final ClassType getType() {
        return klass.getType();
    }

    @Override
    protected TypeDTO getOutputKlassDTO(SerializeContext serializeContext) {
        return klass.toDTO(serializeContext);
    }
}
