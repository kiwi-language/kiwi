package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.Expressions;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.ForeachNodeNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

@EntityType
public class ForeachNode extends LoopNode {

    public static ForeachNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var outputType = ((ClassType) TypeParser.parseType(nodeDTO.outputType(), context)).resolve();
        ParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
        ForeachNodeNodeParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.getArray(), parsingContext);
        Value condition = Values.expression(Expressions.trueExpression());
        if (outputType.findFieldByCode("array") == null) {
            FieldBuilder.newBuilder("array", "array", outputType, array.getType()).build();
        }
        if (outputType.findFieldByCode("index") == null) {
            FieldBuilder.newBuilder("index", "index", outputType, ModelDefRegistry.getType(Long.class)).build();
        }
        // IMPORTANT COMMENT DON"T REMOVE:
        // DO NOT call setLoopParam here. setLoopParam should be called after the loop body has been constructed.
        // See FlowManager.saveLoopNodeContent
        var node = (ForeachNode) context.getNode(Id.parse(nodeDTO.id()));
        if(node == null)
            node = new ForeachNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType, prev, scope, array, condition);
        else
            node.setArray(array);
        return node;
    }

    private Value array;

    public ForeachNode(Long tmpId, String name, @javax.annotation.Nullable String code, @NotNull Klass outputType, NodeRT previous, ScopeRT scope,
                       Value array, Value condition) {
        super(tmpId, name, code, outputType, previous, scope, condition);
        this.array = array;
    }

    @Override
    protected ForeachNodeNodeParam getParam(SerializeContext serializeContext) {
        return new ForeachNodeNodeParam(
                array.toDTO(),
                NncUtils.get(getCondition(), Value::toDTO),
                NncUtils.map(getFields(), LoopField::toDTO),
                getBodyScope().toDTO(true, serializeContext)
        );
    }

    public void setArray(Value array) {
        this.array = array;
    }

    @Override
    protected Map<Field, Instance> getExtraLoopFields(MetaFrame frame) {
        var arrayValue = array.evaluate(frame);
        var index = Instances.longInstance(0);
        return new HashMap<>(Map.of(
                getKlass().getFieldByCode("array"), arrayValue,
                getKlass().getFieldByCode("index"), index
        ));
    }

    @Override
    protected void updateExtraFields(ClassInstance instance, MetaFrame frame) {
        instance.setField(
                getKlass().getFieldByCode("index"),
                instance.getLongField(getKlass().findFieldByCode("index")).inc(1)
        );
    }

    @Override
    protected boolean checkExtraCondition(ClassInstance loopObject, MetaFrame frame) {
        var arrayField = getKlass().findFieldByCode("array");
        var indexField = getKlass().findFieldByCode("index");
        var array = loopObject.getField(arrayField).resolveArray();
        var index = loopObject.getLongField(indexField);
        return index.getValue() < array.length();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitForeachNode(this);
    }
}
