package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.flow.Nodes;
import org.metavm.flow.ScopeRT;
import org.metavm.object.type.Field;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Type;
import org.metavm.object.view.rest.dto.DirectFieldMappingParam;
import org.metavm.util.BusinessException;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@EntityType
public class DirectFieldMapping extends FieldMapping implements LocalKey, GenericElement {

    private FieldRef sourceFieldRef;

    @CopyIgnore
    @Nullable
    private DirectFieldMapping template;

    public DirectFieldMapping(Long tmpId, FieldRef targetFieldRef, FieldsObjectMapping containingMapping,
                              @Nullable NestedMapping nestedMapping, Field sourceField) {
        super(tmpId, targetFieldRef, containingMapping, nestedMapping);
        this.sourceFieldRef = sourceField.getRef();
    }

    @Override
    public DirectFieldMappingParam getParam(SerializeContext serializeContext) {
        return new DirectFieldMappingParam();
    }

    @Override
    @Nullable
    public DirectFieldMapping getCopySource() {
        return template;
    }

    @Override
    public void setCopySource(Object copySource) {
        this.template = (DirectFieldMapping) copySource;
    }

    @Override
    public @NotNull Field getSourceField() {
        return sourceFieldRef.resolve();
    }

    public static boolean checkReadonly(Field sourceField, boolean readonly) {
        if (sourceField.isReadonly() && !readonly)
            throw new BusinessException(ErrorCode.MUTABLE_TARGET_FIELD_FROM_READONLY_SOURCE);
        return readonly;
    }

    @Override
    public Type generateReadCode0(ScopeRT scope) {
        Nodes.thisProperty(getSourceField(), scope);
        return getSourceField().getType();
    }

    @Override
    protected void generateWriteCode0(Supplier<Type> getFieldValue, ScopeRT scope) {
        Nodes.this_(scope);
        getFieldValue.get();
        Nodes.setField(getSourceField(), scope);
    }

    @Override
    protected Type getTargetFieldType() {
        return getSourceField().getType();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDirectFieldMapping(this);
    }

    public void update(Field sourceField, boolean readonly) {
        checkReadonly(sourceField, readonly);
        this.sourceFieldRef = sourceField.getRef();
        setReadonly(readonly);
        resetTargetFieldType();
    }

}
