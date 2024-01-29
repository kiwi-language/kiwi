package tech.metavm.object.view;

import tech.metavm.entity.*;
import tech.metavm.expression.NodeExpression;
import tech.metavm.expression.ThisExpression;
import tech.metavm.flow.SelfNode;
import tech.metavm.flow.Value;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.ComputedFieldMappingParam;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@EntityType("计算视图字段映射")
public class ComputedFieldMapping extends FieldMapping {

    @ChildEntity("值")
    private Value value;

    @EntityField("模板")
    @Nullable
    @CopyIgnore
    private final ComputedFieldMapping template;

    public ComputedFieldMapping(Long tmpId,
                                Field targetField,
                                FieldsObjectMapping containingMapping,
                                @Nullable ObjectMapping nestedMapping,
                                Value value) {
        super(tmpId, targetField, containingMapping, nestedMapping);
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
        return () -> (Value) new CopyVisitor(value) {
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
