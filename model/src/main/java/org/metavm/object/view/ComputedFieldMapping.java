package org.metavm.object.view;

import org.metavm.entity.*;
import org.metavm.expression.NodeExpression;
import org.metavm.expression.ThisExpression;
import org.metavm.flow.SelfNode;
import org.metavm.flow.Value;
import org.metavm.object.type.Field;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Type;
import org.metavm.object.view.rest.dto.ComputedFieldMappingParam;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@EntityType
public class ComputedFieldMapping extends FieldMapping {

    private Value value;
    @Nullable
    @CopyIgnore
    private final ComputedFieldMapping template;

    public ComputedFieldMapping(Long tmpId,
                                FieldRef targetFieldRef,
                                FieldsObjectMapping containingMapping,
                                @Nullable NestedMapping nestedMapping,
                                Value value) {
        super(tmpId, targetFieldRef, containingMapping, nestedMapping);
        this.value = value;
        this.template = null;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitComputedFieldMapping(this);
    }

    @Override
    public ComputedFieldMappingParam getParam(SerializeContext serializeContext) {
        return new ComputedFieldMappingParam(value.toDTO());
    }

    @Override
    protected Type getTargetFieldType() {
        return value.getType();
    }

    @Nullable
    public ComputedFieldMapping getCopySource() {
        return template;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Field getSourceField() {
        return null;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public Supplier<Value> generateReadCode0(SelfNode selfNode) {
        return () -> (Value) new CopyVisitor(value, value.isStrictEphemeral()) {
            @Override
            public Element visitThisExpression(ThisExpression expression) {
                return new NodeExpression(selfNode);
            }
        }.copy(value);
    }

    @Override
    protected void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier) {
        throw new UnsupportedOperationException();
    }

}
