package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.rest.dto.TypeDTO;

import javax.annotation.Nullable;

@EntityType("子类型节点")
public abstract class ChildTypeNode extends NodeRT {

    @ChildEntity("节点类型")
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
