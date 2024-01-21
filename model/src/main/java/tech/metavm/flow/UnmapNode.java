package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UnmapNodeParam;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.Mapping;
import tech.metavm.util.AssertUtils;

@EntityType("反映射节点")
public class UnmapNode extends NodeRT {

    public static UnmapNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (UnmapNode) context.getNode(nodeDTO.getRef());
        var param = (UnmapNodeParam) nodeDTO.param();
        var mapping = context.getMapping(param.mappingRef());
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var view = ValueFactory.create(param.view(), parsingContext);
        if (node == null)
            node = new UnmapNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, view, mapping);
        else
            node.update(view, mapping);
        return node;
    }

    @ChildEntity("视图")
    private Value view;
    @EntityField("映射")
    private Mapping mapping;

    public UnmapNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                     Value view, Mapping mapping) {
        super(tmpId, name, code, null, previous, scope);
        this.view = addChild(view, "view");
        this.mapping = mapping;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnmapNode(this);
    }

    @Override
    protected UnmapNodeParam getParam(SerializeContext serializeContext) {
        return new UnmapNodeParam(view.toDTO(), serializeContext.getRef(mapping));
    }

    public void update(Value view, Mapping mapping) {
        AssertUtils.assertTrue(mapping.getTargetType().isAssignableFrom(view.getType()),
                ErrorCode.INCORRECT_MAPPING);
        this.view = addChild(view, "view");
        this.mapping = mapping;
    }

    @NotNull
    @Override
    public Type getType() {
        return mapping.getSourceType();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var viewInst = (DurableInstance) view.evaluate(frame);
        return next(mapping.unmap(viewInst, frame.getInstanceRepository(), frame.parameterizedFlowProvider()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("unmap " + mapping.getName() + "(" + view.getText() + ")");
    }
}
