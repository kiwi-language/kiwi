package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.InstanceOfNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class InstanceOfNode extends NodeRT {

    public static InstanceOfNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        InstanceOfNode node = (InstanceOfNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            InstanceOfNodeParam param = nodeDTO.getParam();
            var type = TypeParser.parseType(param.type(), context);
            node = new InstanceOfNode(nodeDTO.tmpId(), nodeDTO.name(),
                    prev, scope, type);
        }
        return node;
    }

    private final Type targetType;

    public InstanceOfNode(Long tmpId,
                          @NotNull String name,
                          @Nullable NodeRT previous,
                          @NotNull ScopeRT scope,
                          Type targetType) {
        super(tmpId, name, Types.getBooleanType(), previous, scope);
       this.targetType = targetType;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInstanceOfNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new InstanceOfNodeParam(targetType.toExpression(serializeContext));
    }

    @Override
    public int execute(MetaFrame frame) {
        var v =  frame.pop();
        frame.push(Instances.booleanInstance(targetType.isInstance(v)));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("instanceof");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    public Type getTargetType() {
        return targetType;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

}
