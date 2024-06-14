package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.UnmapNodeParam;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.view.ObjectMappingRef;
import org.metavm.util.AssertUtils;

@EntityType
public class UnmapNode extends NodeRT {

    public static UnmapNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (UnmapNode) context.getNode(Id.parse(nodeDTO.id()));
        var param = (UnmapNodeParam) nodeDTO.param();
        var mappingRef = ObjectMappingRef.create(param.mappingRef(), context);
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var view = ValueFactory.create(param.view(), parsingContext);
        if (node == null)
            node = new UnmapNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, view, mappingRef);
        else
            node.update(view, mappingRef);
        return node;
    }

    private Value view;
    private ObjectMappingRef mappingRef;

    public UnmapNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                     Value view, ObjectMappingRef mappingRef) {
        super(tmpId, name, code, null, previous, scope);
        this.view = view;
        this.mappingRef = mappingRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnmapNode(this);
    }

    @Override
    protected UnmapNodeParam getParam(SerializeContext serializeContext) {
        return new UnmapNodeParam(view.toDTO(), mappingRef.toDTO(serializeContext));
    }

    public void update(Value view, ObjectMappingRef mappingRef) {
        AssertUtils.assertTrue(mappingRef.resolve().getTargetType().isAssignableFrom(view.getType()),
                ErrorCode.INCORRECT_MAPPING);
        this.view = view;
        this.mappingRef = mappingRef;
    }

    @NotNull
    @Override
    public Type getType() {
        return mappingRef.resolve().getSourceType();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var viewInst = (DurableInstance) view.evaluate(frame);
        return next(mappingRef.resolve().unmap(viewInst, frame));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("unmap " + mappingRef.resolve().getName() + "(" + view.getText() + ")");
    }
}
