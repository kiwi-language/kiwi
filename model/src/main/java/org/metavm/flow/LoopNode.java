package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ChildArray;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadonlyArray;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.Expressions;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.LoopFieldDTO;
import org.metavm.flow.rest.LoopNodeParam;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType
public abstract class LoopNode extends ScopeNode {

    @ChildEntity
    private final ChildArray<LoopField> fields = addChild(new ChildArray<>(LoopField.class), "fields");
    private Value condition;
    @ChildEntity
    private final Klass klass;

    protected LoopNode(Long tmpId, String name, @Nullable String code, @NotNull Klass klass, NodeRT previous,
                       @NotNull ScopeRT scope, @NotNull Value condition) {
        super(tmpId, name, code, null, previous, scope, true);
        this.klass = addChild(klass, "klass");
        this.condition = condition;
    }

    public void setLoopParam(LoopNodeParam param, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        List<LoopField> fields = new ArrayList<>();
        for (LoopFieldDTO loopFieldDTO : param.getFields()) {
            var field = context.getField(Id.parse(loopFieldDTO.fieldId()));
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
        this.condition = condition;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return klass.getType();
    }

    public Klass getKlass() {
        return klass;
    }

    @Override
    protected KlassDTO getOutputKlassDTO(SerializeContext serializeContext) {
        return klass.toDTO(serializeContext);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("while (" + condition.getText() + ")");
        bodyScope.writeCode(writer);
        writer.writeNewLine("{" + NncUtils.join(fields, LoopField::getText, ", ") + "}");
    }
}
