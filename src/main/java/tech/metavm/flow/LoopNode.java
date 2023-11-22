package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.LoopFieldDTO;
import tech.metavm.flow.rest.LoopParamDTO;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

import java.util.*;

@EntityType("循环节点")
public abstract class LoopNode<T extends LoopParamDTO> extends ScopeNode<T> {

    @ChildEntity("字段列表")
    private final ChildArray<LoopField> fields = addChild(new ChildArray<>(LoopField.class), "fields");
    @ChildEntity("条件")
    private Value condition;
    @ChildEntity("节点类型")
    private final ClassType nodeType;

    protected LoopNode(Long tmpId, String name, @NotNull ClassType outputType, NodeRT<?> previous,
                       @NotNull ScopeRT scope, @NotNull Value condition) {
        super(tmpId, name, null, previous, scope, true);
        this.nodeType = addChild(outputType, "nodeType");
        this.condition = addChild(condition, "condition");
    }

    @Override
    protected T getParam(boolean persisting) {
        return null;
    }

    protected void setLoopParam(LoopParamDTO param, IEntityContext context) {
        if (param.getFields() != null) {
            updateFields(param.getFields(), context);
        }
        var parsingContext = getParsingContext(context);
        if (param.getCondition() != null) {
            setCondition(ValueFactory.create(param.getCondition(), parsingContext));
        }
    }

    public void updateFields(List<LoopFieldDTO> loopFieldDTOs, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        Set<Field> fields = new HashSet<>();
        for (LoopFieldDTO loopFieldDTO : loopFieldDTOs) {
            var field = context.getField(loopFieldDTO.fieldRef());
            fields.add(field);
            var loopField = this.fields.get(LoopField::getField, field);
            if (loopField == null) {
                this.fields.addChild(new LoopField(
                        field,
                        ValueFactory.create(loopFieldDTO.initialValue(), parsingContext),
                        ValueFactory.create(loopFieldDTO.updatedValue(), parsingContext)
                ));
            } else {
                loopField.update(loopFieldDTO, context, parsingContext);
            }
        }
        this.fields.removeIf(loopField -> !fields.contains(loopField.getField()));
    }

    @Override
    public ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(
                bodyScope, bodyScope.getLastNode(), entityContext.getInstanceContext()
        );
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
    public final void execute(MetaFrame frame) {
        ClassInstance loopObject = (ClassInstance) frame.getResult(this);
        if (loopObject == null) {
            loopObject = initLoopObject(frame);
            frame.setResult(loopObject);
        } else {
            updateLoopObject(loopObject, frame);
        }
        var extraCondValue = (BooleanInstance) condition.evaluate(frame);
        if (!extraCondValue.getValue() || !checkExtraCondition(loopObject, frame)) {
            return;
        }
        if (bodyScope.isEmpty()) {
            frame.jumpTo(this);
        } else {
            frame.jumpTo(bodyScope.getFirstNode());
        }
    }

    private ClassInstance initLoopObject(MetaFrame frame) {
        Map<Field, Instance> fieldValues = new HashMap<>(getExtraLoopFields(frame));
        for (LoopField field : fields) {
            fieldValues.put(field.getField(), field.getInitialValue().evaluate(frame));
        }
        return new ClassInstance(fieldValues, getType());
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

}
