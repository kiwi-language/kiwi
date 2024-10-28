package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
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
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var operand = ValueFactory.create(param.operand(), parsingContext);
            var type = TypeParser.parseType(param.type(), context);
            node = new InstanceOfNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, operand, type);
        }
        return node;
    }

    private final Value operand;
    private final Type targetType;

    public InstanceOfNode(Long tmpId,
                          @NotNull String name,
                          @Nullable String code,
                          @Nullable NodeRT previous,
                          @NotNull ScopeRT scope,
                          Value operand,
                          Type targetType) {
        super(tmpId, name, code, Types.getBooleanType(), previous, scope);
        this.operand = operand;
        this.targetType = targetType;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInstanceOfNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new InstanceOfNodeParam(operand.toDTO(), targetType.toExpression(serializeContext));
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v =  operand.evaluate(frame);
        return next(Instances.booleanInstance(targetType.isInstance(v)));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(operand.getText() + " instanceof " + targetType.toExpression());
    }

    public Value getOperand() {
        return operand;
    }

    public Type getTargetType() {
        return targetType;
    }
}
