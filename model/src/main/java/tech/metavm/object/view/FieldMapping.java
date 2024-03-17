package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.view.rest.dto.FieldMappingDTO;
import tech.metavm.object.view.rest.dto.FieldMappingParam;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType("字段映射")
public abstract class FieldMapping extends Element {

    @EntityField("目标字段")
    private final Field targetField;

    @EntityField("所属映射")
    protected final FieldsObjectMapping containingMapping;

    @ChildEntity("嵌套映射")
    @Nullable
    protected NestedMapping nestedMapping;

    public FieldMapping(Long tmpId, Field targetField, FieldsObjectMapping containingMapping, @Nullable NestedMapping nestedMapping) {
        super(tmpId);
        this.containingMapping = containingMapping;
        this.targetField = targetField;
        this.nestedMapping = NncUtils.get(nestedMapping, c -> addChild(c, "nestedMapping"));
        containingMapping.addField(this);
    }

    public Field getTargetField() {
        return targetField;
    }

    public void setTargetFieldType(Type type) {
        targetField.setType(type);
    }

    public boolean isReadonly() {
        return targetField.isReadonly();
    }

    public String getName() {
        return targetField.getName();
    }

    public @Nullable String getCode() {
        return targetField.getCode();
    }

    public void setName(String name) {
        targetField.setName(name);
    }

    public void setCode(String code) {
        targetField.setCode(code);
    }

    public abstract FieldMapping getCopySource();

    public boolean isChild() {
        return targetField.isChild();
    }

    public FieldMappingDTO toDTO(SerializeContext serializeContext) {
        return new FieldMappingDTO(
                serializeContext.getId(this),
                getName(),
                getCode(),
                serializeContext.getId(getType()),
                isChild(),
                isReadonly(),
                NncUtils.get(getSourceField(), serializeContext::getId),
                serializeContext.getId(targetField),
                nestedMapping instanceof ObjectNestedMapping classCodeGenerator ? classCodeGenerator.getMapping().getStringId() : null,
                getParam(serializeContext)
        );
    }

    public abstract @Nullable Field getSourceField();

    public abstract FieldMappingParam getParam(SerializeContext serializeContext);

    public FieldsObjectMapping getContainingMapping() {
        return containingMapping;
    }

    public Type getType() {
        return targetField.getType();
    }

    public void setReadonly(boolean readonly) {
        targetField.setReadonly(readonly);
    }

    public FieldParam generateReadCode(SelfNode selfNode) {
        var valueSupplier = generateReadCode0(selfNode);
        var value = valueSupplier.get();
        if (nestedMapping != null) {
            var getNestedValue = nestedMapping.generateMappingCode(valueSupplier, selfNode.getScope());
            return new FieldParam(targetField, getNestedValue.get());
        } else
            return new FieldParam(targetField, value);
    }

    protected abstract Supplier<Value> generateReadCode0(SelfNode selfNode);

    public void generateWriteCode(SelfNode selfNode, ValueNode viewNode) {
        if (nestedMapping != null) {
            var scope = selfNode.getScope();
            var nestedFieldSource = nestedMapping.generateUnmappingCode(
                    () -> Values.nodeProperty(viewNode, targetField), scope);
            generateWriteCode0(selfNode, nestedFieldSource);
        } else
            generateWriteCode0(selfNode, () -> Values.nodeProperty(viewNode, targetField));
    }

    protected abstract void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier);

    protected abstract Type getTargetFieldType();

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        return List.of(targetField);
    }

    public void setNestedMapping(@Nullable NestedMapping nestedMapping, CompositeTypeFacade compositeTypeFacade) {
        if(Objects.equals(nestedMapping, this.nestedMapping))
            return;
        this.nestedMapping = NncUtils.get(nestedMapping, c -> addChild(c, "nestedMapping"));
        resetTargetFieldType(compositeTypeFacade);
    }

    protected void resetTargetFieldType(CompositeTypeFacade compositeTypeFacade) {
        targetField.setType(getTargetFieldType(getTargetFieldType(), getNestedMapping(), compositeTypeFacade));
    }

    public static Type getTargetFieldType(Type targetFieldType, @Nullable ObjectMapping nestedMapping, CompositeTypeFacade compositeTypeFacade) {
        if (nestedMapping == null)
            return targetFieldType;
        if (targetFieldType instanceof ArrayType arrayType) {
            var elementType = arrayType.getInnermostElementType();
            var typeSubst = new TypeSubstitutor(List.of(elementType), List.of(nestedMapping.getTargetType()),
                    compositeTypeFacade, new MockDTOProvider());
            return targetFieldType.accept(typeSubst);
        } else {
            return targetFieldType;
        }
    }

    @Nullable
    public ObjectMapping getNestedMapping() {
        return nestedMapping instanceof ObjectNestedMapping classCodeGenerator ? classCodeGenerator.getMapping() : null;
    }

    public NestedMapping nestedMapping() {
        return nestedMapping;
    }

    public boolean isValidLocalKey() {
        return getCode() != null;
    }

    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(getCode());
    }
}
