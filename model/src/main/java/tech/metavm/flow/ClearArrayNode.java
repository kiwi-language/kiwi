package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.ClearArrayNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.util.BusinessException;

import javax.annotation.Nullable;

@EntityType("清空数组节点")
public class ClearArrayNode extends NodeRT {

    public static ClearArrayNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (ClearArrayNodeParam) nodeDTO.param();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var array = ValueFactory.create(param.array(), parsingContext);
        var node = (ClearArrayNode) context.getNode(nodeDTO.getRef());
        if (node == null)
            node = new ClearArrayNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, array);
        else
            node.setArray(array);
        return node;
    }

    @ChildEntity("数组")
    private @NotNull Value array;

    public ClearArrayNode(Long tmpId, String name, @Nullable String code,
                          NodeRT previous, ScopeRT scope, @NotNull Value array) {
        super(tmpId, name, code, null, previous, scope);
        this.array = addChild(check(array), "array");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClearArrayNode(this);
    }

    public void setArray(@NotNull Value array) {
        this.array = addChild(check(array), "array");
    }

    private Value check(@NotNull Value array) {
        if(!array.getType().isArray())
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        return array;
    }

    @Override
    protected ClearArrayNodeParam getParam(SerializeContext serializeContext) {
        return new ClearArrayNodeParam(array.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        arrayInst.clear();
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("clear(" + array.getText() + ")");
    }

}
