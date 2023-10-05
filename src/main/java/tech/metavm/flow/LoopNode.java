package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.LoopFieldDTO;
import tech.metavm.flow.rest.LoopParamDTO;
import tech.metavm.object.instance.BooleanInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.Table;

import java.util.*;

@EntityType("循环节点")
public abstract class LoopNode<T extends LoopParamDTO> extends ScopeNode<T> {

    @ChildEntity("字段列表")
    private final Table<LoopField> fields = new Table<>(LoopField.class, true);
    @ChildEntity("条件")
    private Value condition;

    protected LoopNode(Long tmpId, String name, @Nullable Type outputType, NodeRT<?> previous,
                       ScopeRT scope, Value condition) {
        super(tmpId, name, outputType, previous, scope, true);
        this.condition = condition;
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
            condition = ValueFactory.create(param.getCondition(), parsingContext);
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
                this.fields.add(new LoopField(
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

    public List<LoopField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public void setField(Field field, Value initialValue, Value updatedValue) {
        var loopField = fields.get(LoopField::getField, field);
        if (loopField == null) {
            fields.add(new LoopField(field, initialValue, updatedValue));
        } else {
            loopField.setInitialValue(initialValue);
            loopField.setUpdatedValue(updatedValue);
        }
    }

    @Override
    public final void execute(FlowFrame frame) {
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

    private ClassInstance initLoopObject(FlowFrame frame) {
        Map<Field, Instance> fieldValues = new HashMap<>(getExtraLoopFields(frame));
        for (LoopField field : fields) {
            fieldValues.put(field.getField(), field.getInitialValue().evaluate(frame));
        }
        return new ClassInstance(fieldValues, getType());
    }

    private void updateLoopObject(ClassInstance loopObject, FlowFrame frame) {
        updateExtraFields(loopObject, frame);
        for (LoopField field : fields) {
            loopObject.set(field.getField(), field.getUpdatedValue().evaluate(frame));
        }
    }

    protected Map<Field, Instance> getExtraLoopFields(FlowFrame frame) {
        return Map.of();
    }

    protected void updateExtraFields(ClassInstance instance, FlowFrame frame) {
    }

    protected boolean checkExtraCondition(ClassInstance loopObject, FlowFrame frame) {
        return true;
    }

    public Value getCondition() {
        return condition;
    }

    public void setCondition(@Nullable Value condition) {
        this.condition = condition;
    }


    public ScopeRT getBodyScope() {
        return bodyScope;
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    @Override
    protected List<Object> nodeBeforeRemove() {
        return List.of(getType());
    }


}
