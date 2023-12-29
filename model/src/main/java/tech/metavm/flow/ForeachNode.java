package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.ForeachNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

@EntityType("Foreach循环节点")
public class ForeachNode extends LoopNode {

    public static ForeachNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var outputType = context.getClassType(nodeDTO.outputTypeRef());
        ParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        ForeachNodeParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.getArray(), parsingContext);
        Value condition = Values.expression(Expressions.trueExpression());
        if (outputType.findFieldByCode("array") == null) {
            FieldBuilder.newBuilder("数组", "array", outputType, array.getType()).build();
        }
        if (outputType.findFieldByCode("index") == null) {
            FieldBuilder.newBuilder("索引", "index", outputType, ModelDefRegistry.getType(Long.class)).build();
        }
        // IMPORTANT COMMENT DON"T REMOVE:
        // DO NOT call setLoopParam here. setLoopParam should be called after the loop body has been constructed.
        // See FlowManager.saveLoopNodeContent
        var node = (ForeachNode) context.getNode(nodeDTO.getRef());
        if(node == null)
            node = new ForeachNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType, prev, scope, array, condition);
        else
            node.setArray(array);
        return node;
    }

    @ChildEntity("数组")
    private Value array;

    public ForeachNode(Long tmpId, String name, @javax.annotation.Nullable String code, @NotNull ClassType outputType, NodeRT previous, ScopeRT scope,
                       Value array, Value condition) {
        super(tmpId, name, code, outputType, previous, scope, condition);
        setArray(array);
    }

    @Override
    protected ForeachNodeParam getParam(SerializeContext serializeContext) {
        return new ForeachNodeParam(
                array.toDTO(),
                NncUtils.get(getCondition(), Value::toDTO),
                NncUtils.map(getFields(), LoopField::toDTO),
                getBodyScope().toDTO(true, serializeContext)
        );
    }

    public void setArray(Value array) {
        this.array = addChild(array, "array");
    }

    @Override
    protected Map<Field, Instance> getExtraLoopFields(MetaFrame frame) {
        var arrayValue = array.evaluate(frame);
        var index = Instances.longInstance(0);
        return new HashMap<>(Map.of(
                getType().getFieldByCode("array"), arrayValue,
                getType().getFieldByCode("index"), index
        ));
    }

    @Override
    protected void updateExtraFields(ClassInstance instance, MetaFrame frame) {
        instance.setField(
                getType().getFieldByCode("index"),
                instance.getLongField(getType().findFieldByCode("index")).inc(1)
        );
    }

    @Override
    protected boolean checkExtraCondition(ClassInstance loopObject, MetaFrame frame) {
        var arrayField = getType().findFieldByCode("array");
        var indexField = getType().findFieldByCode("index");
        var array = (ArrayInstance) loopObject.getField(arrayField);
        var index = loopObject.getLongField(indexField);
        return index.getValue() < array.length();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitForeachNode(this);
    }
}
