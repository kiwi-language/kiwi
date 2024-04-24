package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;

import javax.annotation.Nullable;

@EntityType("子类型节点")
public abstract class ChildTypeNode extends NodeRT {

    @ChildEntity("节点类型")
    private final Klass klass;

    protected ChildTypeNode(Long tmpId, String name, @Nullable String code, Klass klass, NodeRT previous, ScopeRT scope) {
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


}
