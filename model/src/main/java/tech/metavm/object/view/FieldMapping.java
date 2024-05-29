package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldRef;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.FieldMappingDTO;
import tech.metavm.object.view.rest.dto.FieldMappingParam;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType("字段映射")
public abstract class FieldMapping extends Element {

    @ChildEntity("目标字段引用")
    private final FieldRef targetFieldRef;

    @EntityField("所属映射")
    protected final FieldsObjectMapping containingMapping;

    @ChildEntity("嵌套映射")
    @Nullable
    protected NestedMapping nestedMapping;

    public FieldMapping(Long tmpId, FieldRef targetFieldRef, FieldsObjectMapping containingMapping, @Nullable NestedMapping nestedMapping) {
        super(tmpId);
        this.containingMapping = containingMapping;
        this.targetFieldRef = addChild(targetFieldRef.copy(), "targetFieldRef");
        this.nestedMapping = NncUtils.get(nestedMapping, c -> addChild(c, "nestedMapping"));
        containingMapping.addField(this);
    }

    public Field getTargetField() {
        return targetFieldRef.resolve();
    }

    public void setTargetFieldType(Type type) {
        targetFieldRef.resolve().setType(type);
    }

    public boolean isReadonly() {
        return getTargetField().isReadonly();
    }

    public String getName() {
        return getTargetField().getName();
    }

    public @Nullable String getCode() {
        return getTargetField().getCode();
    }

    public void setName(String name) {
        getTargetField().setName(name);
    }

    public void setCode(String code) {
        getTargetField().setCode(code);
    }

    public abstract FieldMapping getCopySource();

    public boolean isChild() {
        return getTargetField().isChild();
    }

    public FieldMappingDTO toDTO(SerializeContext serializeContext) {
        return new FieldMappingDTO(
                serializeContext.getStringId(this),
                getName(),
                getCode(),
                getType().toExpression(),
                isChild(),
                isReadonly(),
                NncUtils.get(getSourceField(), serializeContext::getStringId),
                targetFieldRef.toDTO(serializeContext),
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
        return getTargetField().getType();
    }

    public void setReadonly(boolean readonly) {
        getTargetField().setReadonly(readonly);
    }

    public FieldParam generateReadCode(SelfNode selfNode) {
        var valueSupplier = generateReadCode0(selfNode);
        var value = valueSupplier.get();
        if (nestedMapping != null) {
            var getNestedValue = nestedMapping.generateMappingCode(valueSupplier, selfNode.getScope());
            return new FieldParam(targetFieldRef, getNestedValue.get());
        } else
            return new FieldParam(targetFieldRef, value);
    }

    protected abstract Supplier<Value> generateReadCode0(SelfNode selfNode);

    public void generateWriteCode(SelfNode selfNode, ValueNode viewNode) {
        if (nestedMapping != null) {
            var scope = selfNode.getScope();
            var nestedFieldSource = nestedMapping.generateUnmappingCode(
                    () -> Values.nodeProperty(viewNode, getTargetField()), scope);
            generateWriteCode0(selfNode, nestedFieldSource);
        } else
            generateWriteCode0(selfNode, () -> Values.nodeProperty(viewNode, targetFieldRef.resolve()));
    }

    protected abstract void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier);

    protected abstract Type getTargetFieldType();

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        return List.of(targetFieldRef);
    }

    public void setNestedMapping(@Nullable NestedMapping nestedMapping) {
        if (Objects.equals(nestedMapping, this.nestedMapping))
            return;
        this.nestedMapping = NncUtils.get(nestedMapping, c -> addChild(c, "nestedMapping"));
        resetTargetFieldType();
    }

    protected void resetTargetFieldType() {
        getTargetField().setType(getTargetFieldType(getTargetFieldType(), this.nestedMapping));
    }

    public static Type getTargetFieldType(Type targetFieldType, @Nullable NestedMapping nestedMapping) {
        if (nestedMapping == null)
            return targetFieldType;
        else
            return nestedMapping.getTargetType();
    }

    @Nullable
    public NestedMapping getNestedMapping() {
        return nestedMapping;
    }

    public boolean isValidLocalKey() {
        return getCode() != null;
    }

    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(getCode());
    }

    public String getText() {
        return "{\"sourceField\": " + NncUtils.get(getSourceField(), f -> "\"" + f.getName() + "\"")
                + ", \"targetField\": \"" + getTargetField().getName()
                + "\", \"nestedMapping\": " + NncUtils.get(nestedMapping, NestedMapping::getText) + "}";
    }

}
