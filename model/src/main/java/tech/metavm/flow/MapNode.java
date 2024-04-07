package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.MapNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.Mapping;
import tech.metavm.util.AssertUtils;

import java.util.Map;

@EntityType("映射节点")
public class MapNode extends NodeRT {

    public static MapNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (MapNode) context.getNode(Id.parse(nodeDTO.id()));
        var param = (MapNodeParam) nodeDTO.param();
        var mapping = context.getMapping(Id.parse(param.mappingId()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var source = ValueFactory.create(param.source(), parsingContext);
        if (node == null)
            node = new MapNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, source, mapping);
        else
            node.update(source, mapping);
        return node;
    }

    @ChildEntity("来源")
    private Value source;
    @EntityField("映射")
    private Mapping mapping;

    public MapNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                   Value source, Mapping mapping) {
        super(tmpId, name, code, null, previous, scope);
        this.source = addChild(source, "source");
        this.mapping = mapping;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMapNode(this);
    }

    @Override
    protected MapNodeParam getParam(SerializeContext serializeContext) {
        return new MapNodeParam(source.toDTO(), serializeContext.getId(mapping));
    }

    public void update(Value source, Mapping mapping) {
        AssertUtils.assertTrue(mapping.getSourceType().isAssignableFrom(source.getType(), Map.of()),
                ErrorCode.INCORRECT_MAPPING);
        this.source = addChild(source, "source");
        this.mapping = mapping;
    }

    @NotNull
    @Override
    public Type getType() {
        return mapping.getTargetType();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var sourceInst = (DurableInstance) source.evaluate(frame);
        return next(mapping.map(sourceInst, frame));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("map " + mapping.getName() + "(" + source.getText() + ")");
    }

}
