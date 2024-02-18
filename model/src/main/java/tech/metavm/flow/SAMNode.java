package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.SAMNodeParam;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.FunctionInstance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.SAMImplType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

@EntityType("SAM节点")
public class SAMNode extends NodeRT {

    public static SAMNode save(NodeDTO nodeDTO, @Nullable NodeRT prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        SAMNodeParam param = nodeDTO.getParam();
        var samInterface = context.getClassType(param.samInterfaceRef());
        var function = ValueFactory.create(param.function(), parsingContext);
        SAMNode node = (SAMNode) context.getNode(nodeDTO.getRef());
        if (node != null) {
            node.setFunction(function);
        } else
            node = new SAMNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), samInterface, prev, scope, function);
        return node;
    }

    @ChildEntity("函数")
    private Value function;

    public SAMNode(Long tmpId, @NotNull String name, @Nullable String code, @NotNull ClassType type, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                      Value function) {
        super(tmpId, name, code, type, previous, scope);
        NncUtils.requireTrue(type.isSAMInterface());
        this.function = addChild(function, "function");
    }

    public void setFunction(Value function) {
        this.function = addChild(function, "function");
    }

    @NotNull
    @Override
    public ClassType getType() {
        return (ClassType) Objects.requireNonNull(super.getType());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSAMNode(this);
    }

    @Override
    protected SAMNodeParam getParam(SerializeContext serializeContext) {
        return new SAMNodeParam(
                serializeContext.getRef(getType()),
                function.toDTO()
        );
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var type = new SAMImplType(getType(), (FunctionInstance) function.evaluate(frame));
        var samInstance = new ClassInstance(null, Map.of(), type);
        return next(samInstance);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("sam(" + getType().getName() + ", " + function.getText() + ")");
    }
}
