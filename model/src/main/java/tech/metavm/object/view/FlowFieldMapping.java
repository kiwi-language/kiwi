package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.FlowFieldMappingParam;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType("流程视图字段")
public class FlowFieldMapping extends FieldMapping implements LocalKey, GenericElement {

    @EntityField("读取流程")
    private Method getter;
    @EntityField("写入流程")
    @Nullable
    private Method setter;
    @EntityField("模板")
    @CopyIgnore
    @Nullable
    private FlowFieldMapping template;

    public FlowFieldMapping(Long tmpId,
                            String name,
                            @Nullable String code,
                            FieldsObjectMapping containingMapping,
                            @Nullable Mapping nestedMapping,
                            boolean isChild,
                            Method getter,
                            @Nullable Method setter,
                            @Nullable FlowFieldMapping template) {
        super(tmpId, name, code, getter.getReturnType(), false, isChild, setter == null, containingMapping, nestedMapping);
        check(getter, setter);
        this.getter = getter;
        this.setter = setter;
        this.template = template;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFlowFieldMapping(this);
    }

    @Override
    public @Nullable FlowFieldMapping getTemplate() {
        return template;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Field getSourceField() {
        return null;
    }

    @Override
    public void setTemplate(Object template) {
        NncUtils.requireNull(this.template);
        this.template = (FlowFieldMapping) template;
    }

    @Override
    public FlowFieldMappingParam getParam(SerializeContext serializeContext) {
        return new FlowFieldMappingParam(
                serializeContext.getRef(getter),
                NncUtils.get(setter, serializeContext::getRef)
        );
    }

    @Override
    public Value generateReadCode0(SelfNode selfNode) {
        var node = new MethodCallNode(
                null,
                getter.getName(),
                getter.getCode(),
                selfNode.getScope().getLastNode(),
                selfNode.getScope(),
                Values.node(selfNode),
                getter,
                List.of()
        );
        return Values.node(node);
    }

    @Override
    protected void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier) {
        Objects.requireNonNull(setter);
        new MethodCallNode(
                null,
                setter.getName(),
                setter.getCode(),
                selfNode.getScope().getLastNode(),
                selfNode.getScope(),
                Values.node(selfNode),
                setter, List.of(Nodes.argument(setter, 0, fieldValueSupplier.get()))
        );
    }

    @Override
    protected Type getTargetFieldType() {
        return getter.getReturnType();
    }

    public void setFlows(Method getter, @Nullable Method setter) {
        check(getter, setter);
        this.getter = getter;
        this.setter = setter;
        setReadonly(setter == null);
        resetTargetFieldType();
    }

    private void check(Flow getter, @Nullable Flow setter) {
        if (!getter.getParameters().isEmpty() || getter.getReturnType().isVoid())
            throw new BusinessException(ErrorCode.INVALID_GETTER_FLOW);
        if (setter != null) {
            var paramTypes = setter.getParameterTypes();
            if (paramTypes.size() != 1
                    || paramTypes.get(0).equals(getter.getType()))
                throw new BusinessException(ErrorCode.INVALID_SETTER_FLOW);
        }
    }

    public Flow getGetter() {
        return getter;
    }

    @Nullable
    public Flow getSetter() {
        return setter;
    }

    @Override
    public boolean isValidLocalKey() {
        return getter.getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getter.getCodeRequired();
    }

}
