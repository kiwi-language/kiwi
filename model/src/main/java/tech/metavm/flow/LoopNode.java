package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.LoopFieldDTO;
import tech.metavm.flow.rest.LoopParamDTO;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType("循环节点")
public abstract class LoopNode extends ScopeNode {

    @ChildEntity("字段列表")
    private final ChildArray<LoopField> fields = addChild(new ChildArray<>(LoopField.class), "fields");
    @ChildEntity("条件")
    private Value condition;
    @ChildEntity("节点类型")
    private final ClassType nodeType;

    protected LoopNode(Long tmpId, String name, @Nullable String code, @NotNull ClassType outputType, NodeRT previous,
                       @NotNull ScopeRT scope, @NotNull Value condition) {
        super(tmpId, name, code, null, previous, scope, true);
        this.nodeType = addChild(outputType, "nodeType");
        this.condition = addChild(condition, "condition");
    }

    public void setLoopParam(LoopParamDTO param, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        List<LoopField> fields = new ArrayList<>();
        for (LoopFieldDTO loopFieldDTO : param.getFields()) {
            var field = context.getField(loopFieldDTO.fieldRef());
            var loopField = this.fields.get(LoopField::getField, field);
            if (loopField == null) {
                fields.add(new LoopField(
                        field,
                        ValueFactory.create(loopFieldDTO.initialValue(), parsingContext),
                        ValueFactory.create(loopFieldDTO.updatedValue(), parsingContext)
                ));
            } else {
                fields.add(loopField);
                loopField.update(loopFieldDTO, parsingContext);
            }
        }
        this.fields.resetChildren(fields);
        var condition = param.getCondition() != null ? ValueFactory.create(param.getCondition(), parsingContext)
                : Values.expression(Expressions.trueExpression());
        setCondition(condition);
    }

    @Override
    public ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(bodyScope, bodyScope.getLastNode(), entityContext);
    }

    public ReadonlyArray<LoopField> getFields() {
        return fields;
    }

    public void setField(Field field, Value initialValue, Value updatedValue) {
        var loopField = fields.get(LoopField::getField, field);
        if (loopField == null) {
            fields.addChild(new LoopField(field, initialValue, updatedValue));
        } else {
            loopField.setInitialValue(initialValue);
            loopField.setUpdatedValue(updatedValue);
        }
    }

    @Override
    public final NodeExecResult execute(MetaFrame frame) {
        ClassInstance loopObject = (ClassInstance) frame.getOutput(this);
        if (!frame.isLooping(this)) {
            frame.enterLoop(this);
            loopObject = initLoopObject(frame);
            frame.setOutput(this, loopObject);
        } else {
            updateLoopObject(loopObject, frame);
        }
        var extraCondValue = (BooleanInstance) condition.evaluate(frame);
        if (!extraCondValue.getValue() || !checkExtraCondition(loopObject, frame)) {
            frame.exitLoop(this);
            return next(loopObject);
        }
        if (bodyScope.isEmpty()) {
            return NodeExecResult.jump(loopObject, this);
        } else {
            return NodeExecResult.jump(loopObject, bodyScope.tryGetFirstNode());
        }
    }

    private ClassInstance initLoopObject(MetaFrame frame) {
        Map<Field, Instance> fieldValues = new HashMap<>(getExtraLoopFields(frame));
        for (LoopField field : fields) {
            fieldValues.put(field.getField(), field.getInitialValue().evaluate(frame));
        }
        return ClassInstance.create(fieldValues, getType());
    }

    private void updateLoopObject(ClassInstance loopObject, MetaFrame frame) {
        updateExtraFields(loopObject, frame);
        for (LoopField field : fields) {
            loopObject.setField(field.getField(), field.getUpdatedValue().evaluate(frame));
        }
    }

    protected Map<Field, Instance> getExtraLoopFields(MetaFrame frame) {
        return Map.of();
    }

    protected void updateExtraFields(ClassInstance instance, MetaFrame frame) {
    }

    protected boolean checkExtraCondition(ClassInstance loopObject, MetaFrame frame) {
        return true;
    }

    public Value getCondition() {
        return condition;
    }

    public void setCondition(@Nullable Value condition) {
        this.condition = condition != null ? addChild(condition, "condition") : null;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return nodeType;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("while (" + condition.getText() + ")");
        bodyScope.writeCode(writer);
        writer.writeNewLine("{" + NncUtils.join(fields, LoopField::getText, ", ") + "}");
    }
}
