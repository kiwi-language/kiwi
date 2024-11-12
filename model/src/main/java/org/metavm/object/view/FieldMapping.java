package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Element;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Nodes;
import org.metavm.flow.ScopeRT;
import org.metavm.object.type.Field;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Type;
import org.metavm.object.view.rest.dto.FieldMappingDTO;
import org.metavm.object.view.rest.dto.FieldMappingParam;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public abstract class FieldMapping extends Element {

    private final FieldRef targetFieldRef;

    protected final FieldsObjectMapping containingMapping;

    @ChildEntity
    @Nullable
    protected NestedMapping nestedMapping;

    public FieldMapping(Long tmpId, FieldRef targetFieldRef, FieldsObjectMapping containingMapping, @Nullable NestedMapping nestedMapping) {
        super(tmpId);
        this.containingMapping = containingMapping;
        this.targetFieldRef = targetFieldRef;
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
                NncUtils.get(getSourceField(), f -> f.getRef().toDTO(serializeContext)),
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

    public void generateReadCode(ScopeRT scope) {
        var t = generateReadCode0(scope);
        if (nestedMapping != null) {
            var viewVar = scope.nextVariableIndex();
            Nodes.store(viewVar, scope);
            nestedMapping.generateMappingCode(() -> Nodes.load(viewVar, t, scope), scope);
        }
    }

    protected abstract Type generateReadCode0(ScopeRT scope);

    public void generateWriteCode(Supplier<Type> getView, ScopeRT scope) {
        getView.get();
        Nodes.getProperty(getTargetField(), scope);
        var fieldType = getTargetField().getType();
        var fieldVar = scope.nextVariableIndex();
        Nodes.store(fieldVar, scope);
        if (nestedMapping != null) {
            var nestedFieldType = nestedMapping.generateUnmappingCode(() -> Nodes.load(fieldVar, fieldType, scope), scope);
            var nestedFieldVar = scope.nextVariableIndex();
            Nodes.store(nestedFieldVar, scope);
            generateWriteCode0(() -> {
                Nodes.load(nestedFieldVar, nestedFieldType, scope);
                return nestedFieldType;
            }, scope);
        } else
            generateWriteCode0(() -> {
                Nodes.load(fieldVar, fieldType, scope);
                return fieldType;
            }, scope);
    }

    protected abstract void generateWriteCode0(Supplier<Type> getFieldValue, ScopeRT scope);

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
