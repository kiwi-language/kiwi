package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.ForEachParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

@EntityType("Foreach节点")
public class ForeachNode extends LoopNode<ForEachParamDTO> {

    public static ForeachNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var outputType = context.getClassType(nodeDTO.outputTypeRef());
        ParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        ForEachParamDTO param = nodeDTO.getParam();
        var array = ValueFactory.create(param.getArray(), parsingContext);
        Value extraCond = Value.expression(ExpressionUtil.trueExpression());
        if(outputType.findFieldByCode("array") == null) {
            FieldBuilder.newBuilder("数组", "array", outputType, array.getType()).build();
        }
        if(outputType.findFieldByCode("index") == null) {
            FieldBuilder.newBuilder("索引", "index", outputType, ModelDefRegistry.getType(Long.class)).build();
        }
        // IMPORTANT COMMENT DON"T REMOVE:
        // DO NOT call setParam here. setParam should be called after the loop body has been constructed.
        // See FlowManager.saveLoopNodeContent
        var node = new ForeachNode(nodeDTO.tmpId(), nodeDTO.name(), outputType, prev, scope, array, extraCond);
        parsingContext = FlowParsingContext.create(scope, node, context);
        extraCond = ValueFactory.create(param.getCondition(), parsingContext);
        node.setCondition(extraCond);
        return node;
    }

    @ChildEntity("数组")
    private Value array;

    public ForeachNode(Long tmpId, String name, @Nullable ClassType outputType, NodeRT<?> previous, ScopeRT scope,
                       Value array, Value condition) {
        super(tmpId, name, outputType, previous, scope, condition);
        setArray(array);
    }

    @Override
    protected ForEachParamDTO getParam(boolean persisting) {
        return new ForEachParamDTO(
                array.toDTO(persisting),
                NncUtils.get(getCondition(), cond -> cond.toDTO(persisting)),
                NncUtils.map(getFields(), field -> field.toDTO(persisting)),
                getBodyScope().toDTO(!persisting)
        );
    }

    @Override
    protected void setParam(ForEachParamDTO param, IEntityContext context) {
        setLoopParam(param, context);
        var parsingContext = getParsingContext(context);
        if (param.getArray() != null) {
            setArray(ValueFactory.create(param.getArray(), parsingContext));
        }
    }

    public void setArray(Value array) {
        this.array = addChild(array, "array");
    }

    @Override
    protected Map<Field, Instance> getExtraLoopFields(MetaFrame frame) {
        var arrayValue = array.evaluate(frame);
        var index = InstanceUtils.longInstance(0);
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
