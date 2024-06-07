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
import tech.metavm.object.view.ObjectMappingRef;
import tech.metavm.util.AssertUtils;

@EntityType
public class MapNode extends NodeRT {

    public static MapNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (MapNode) context.getNode(Id.parse(nodeDTO.id()));
        var param = (MapNodeParam) nodeDTO.param();
        var mappingRef = ObjectMappingRef.create(param.mappingRef(), context);
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var source = ValueFactory.create(param.source(), parsingContext);
        if (node == null)
            node = new MapNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, source, mappingRef);
        else
            node.update(source, mappingRef);
        return node;
    }

    private Value source;
    private ObjectMappingRef mappingRef;

    public MapNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                   Value source, ObjectMappingRef mappingRef) {
        super(tmpId, name, code, null, previous, scope);
        this.source = source;
        this.mappingRef = mappingRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMapNode(this);
    }

    @Override
    protected MapNodeParam getParam(SerializeContext serializeContext) {
        return new MapNodeParam(source.toDTO(), mappingRef.toDTO(serializeContext));
    }

    public void update(Value source, ObjectMappingRef mappingRef) {
        AssertUtils.assertTrue(mappingRef.resolve().getSourceType().isAssignableFrom(source.getType()),
                ErrorCode.INCORRECT_MAPPING);
        this.source = source;
        this.mappingRef = mappingRef;
    }

    @NotNull
    @Override
    public Type getType() {
        return mappingRef.resolve().getTargetType();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var sourceInst = (DurableInstance) source.evaluate(frame);
        return next(mappingRef.resolve().map(sourceInst, frame));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("map " + mappingRef.resolve().getName() + "(" + source.getText() + ")");
    }

}
