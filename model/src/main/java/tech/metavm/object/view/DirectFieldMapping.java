package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.DirectFieldMappingParam;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@EntityType("直接视图字段")
public class DirectFieldMapping extends FieldMapping implements LocalKey, GenericElement {

    @EntityField("源头字段")
    private Field sourceField;

    @EntityField("模板")
    @CopyIgnore
    @Nullable
    private DirectFieldMapping template;

    public DirectFieldMapping(Long tmpId, String name, @Nullable String code, boolean isChild,
                              boolean readonly, FieldsObjectMapping containingMapping, @Nullable Mapping nestedMapping,
                              Field sourceField, @Nullable DirectFieldMapping template) {
        super(tmpId, name, code, sourceField.getType(), isChild, checkReadonly(sourceField, readonly), containingMapping, nestedMapping);
        this.sourceField = sourceField;
        this.template = template;
    }

    public DirectFieldMapping(Long tmpId, Field targetField, FieldsObjectMapping declaringMapping,
                              @Nullable Mapping mapping, Field sourceField) {
        super(tmpId, targetField, declaringMapping, mapping);
        this.sourceField = sourceField;
    }

    @Override
    public DirectFieldMappingParam getParam(SerializeContext serializeContext) {
        return new DirectFieldMappingParam();
    }

    @Override
    @Nullable
    public DirectFieldMapping getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(Object template) {
        this.template = (DirectFieldMapping) template;
    }

    @Override
    public @NotNull Field getSourceField() {
        return sourceField;
    }

    private static boolean checkReadonly(Field sourceField, boolean readonly) {
        if (sourceField.isReadonly() && !readonly)
            throw new BusinessException(ErrorCode.MUTABLE_TARGET_FIELD_FROM_READONLY_SOURCE);
        return readonly;
    }

    @Override
    public Value generateReadCode0(SelfNode selfNode) {
        return Values.nodeProperty(selfNode, sourceField);
    }

    @Override
    protected void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier) {
        var scope = selfNode.getScope();
        var updateNode = new UpdateObjectNode(null, "更新" + sourceField.getName(),
                NamingUtils.tryAddPrefix(sourceField.getCode(), "update"), scope.getLastNode(), scope, Values.node(selfNode));
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
