package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldRef;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.DirectFieldMappingParam;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@EntityType
public class DirectFieldMapping extends FieldMapping implements LocalKey, GenericElement {

    private Field sourceField;

    @CopyIgnore
    @Nullable
    private DirectFieldMapping template;

    public DirectFieldMapping(Long tmpId, FieldRef targetFieldRef, FieldsObjectMapping containingMapping,
                              @Nullable NestedMapping nestedMapping, Field sourceField) {
        super(tmpId, targetFieldRef, containingMapping, nestedMapping);
        this.sourceField = sourceField;
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
        return sourceField;
    }

    public static boolean checkReadonly(Field sourceField, boolean readonly) {
        if (sourceField.isReadonly() && !readonly)
            throw new BusinessException(ErrorCode.MUTABLE_TARGET_FIELD_FROM_READONLY_SOURCE);
        return readonly;
    }

    @Override
    public Supplier<Value> generateReadCode0(SelfNode selfNode) {
        return () -> Values.nodeProperty(selfNode, sourceField);
    }

    @Override
    protected void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier) {
        var scope = selfNode.getScope();
        var updateNode = new UpdateObjectNode(null, "update " + sourceField.getName(),
                NamingUtils.tryAddPrefix(sourceField.getCode(), "update"), scope.getLastNode(), scope, Values.node(selfNode), List.of());
        updateNode.setUpdateField(sourceField, UpdateOp.SET, fieldValueSupplier.get());
    }

    @Override
    protected Type getTargetFieldType() {
        return sourceField.getType();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDirectFieldMapping(this);
    }

    public void update(Field sourceField, boolean readonly) {
        checkReadonly(sourceField, readonly);
        this.sourceField = sourceField;
        setReadonly(readonly);
        resetTargetFieldType();
    }

}
