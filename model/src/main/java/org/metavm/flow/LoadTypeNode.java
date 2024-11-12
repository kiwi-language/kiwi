package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.flow.rest.LoadTypeNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.Types;
import org.metavm.util.ContextUtil;

import javax.annotation.Nullable;

public class LoadTypeNode extends NodeRT {

    public static LoadTypeNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (LoadTypeNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (LoadTypeNodeParam) nodeDTO.getParam();
            var type = TypeParser.parseType(param.type(), context);
            node = new LoadTypeNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, type);
        }
        return node;
    }

    private final Type type;

    public LoadTypeNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope, Type type) {
        super(tmpId, name, null, previous, scope);
        this.type = type;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadTypeNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LoadTypeNodeParam(type.toExpression(serializeContext));
    }

    @Override
    public int execute(MetaFrame frame) {
        var klass = Types.getKlass(type);
        frame.push(ContextUtil.getEntityContext().getInstance(klass.getEffectiveTemplate()).getReference());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("loadType " + type.toExpression());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @NotNull
    @Override
    public Type getType() {
        return StdKlass.type.type();
    }
}
