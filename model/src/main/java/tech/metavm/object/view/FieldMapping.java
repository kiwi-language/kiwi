package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
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

    @EntityField("目标字段")
    private final Field targetField;

    @EntityField("所属映射")
    protected final FieldsObjectMapping containingMapping;

    @EntityField("嵌套映射")
    @Nullable
    protected Mapping nestedMapping;

    public FieldMapping(Long tmpId, Field targetField, FieldsObjectMapping containingMapping, @Nullable Mapping nestedMapping) {
        super(tmpId);
        this.containingMapping = containingMapping;
        this.targetField = targetField;
        this.nestedMapping = nestedMapping;
        containingMapping.addField(this);
    }

    public Field getTargetField() {
        return targetField;
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
                tryGetId(),
                serializeContext.getTmpId(this),
                getName(),
                getCode(),
                serializeContext.getRef(getType()),
                isChild(),
                isReadonly(),
                NncUtils.get(getSourceField(), serializeContext::getRef),
                serializeContext.getRef(targetField),
                NncUtils.get(nestedMapping, serializeContext::getRef),
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
        var value = generateReadCode0(selfNode);
        if (nestedMapping != null) {
//            var nestedFieldView = new FunctionCallNode(
//                    null,
//                    targetField.getName() + "嵌套视图",
//                    NncUtils.get(targetField.getCode(), c -> c + "NestedView"),
//                    selfNode.getScope().getLastNode(),
//                    selfNode.getScope(),
//                    nestedMapping.getMapper(),
//                    List.of(Nodes.argument(nestedMapping.getMapper(), 0, value))
//            );
            var nestedFieldView = new MapNode(
                    null,
                    targetField.getName() + "嵌套视图",
                    NncUtils.get(targetField.getCode(), c -> c + "NestedView"),
                    selfNode.getScope().getLastNode(),
                    selfNode.getScope(),
                    value,
                    nestedMapping
            );
            return new FieldParam(targetField, Values.node(nestedFieldView));
        } else
            return new FieldParam(targetField, value);
    }

    protected abstract Value generateReadCode0(SelfNode selfNode);

    public void generateWriteCode(SelfNode selfNode, ValueNode viewNode) {
        if (nestedMapping != null) {
            var scope = selfNode.getScope();
//            var nestedFieldSource = new FunctionCallNode(null, targetField.getName() + "嵌套来源",
//                    NncUtils.get(targetField.getCode(), c -> c + "NestedSource"),
//                    scope.getLastNode(), scope, nestedMapping.getUnmapper(), List.of(
//                    Nodes.argument(nestedMapping.getUnmapper(), 0, Values.nodeProperty(viewNode, targetField)))
//            );
            var nestedFieldSource = new UnmapNode(null,
                    targetField.getName() + "嵌套来源",
                    NncUtils.get(targetField.getCode(), c -> c + "NestedSource"),
                    scope.getLastNode(),
                    scope,
                    Values.nodeProperty(viewNode, targetField),
                    nestedMapping
            );
            generateWriteCode0(selfNode, () -> Values.node(nestedFieldSource));
        } else
            generateWriteCode0(selfNode, () -> Values.nodeProperty(viewNode, targetField));
    }

    protected abstract void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier);

    protected abstract Type getTargetFieldType();

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        return List.of(targetField);
    }

    public void setNestedMapping(@Nullable Mapping nestedMapping) {
        this.nestedMapping = nestedMapping;
        resetTargetFieldType();
    }

    protected void resetTargetFieldType() {
        targetField.setType(nestedMapping != null ? nestedMapping.getTargetType() : getTargetFieldType());
    }

    @Nullable
    public Mapping getNestedMapping() {
        return nestedMapping;
    }

    public boolean isValidLocalKey() {
        return getCode() != null;
    }

    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(getCode());
    }
}
