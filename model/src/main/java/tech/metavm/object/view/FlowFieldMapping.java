package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.CompositeTypeFacade;
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
    private FlowFieldMapping copySource;

    public FlowFieldMapping(Long tmpId,
                            FieldsObjectMapping containingMapping,
                            @Nullable ObjectMapping nestedMapping,
                            Field targetField,
                            Method getter,
                            @Nullable Method setter,
                            @Nullable FlowFieldMapping copySource) {
        super(tmpId, targetField, containingMapping, nestedMapping);
        check(getter, setter);
        this.getter = getter;
        this.setter = setter;
        this.copySource = copySource;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFlowFieldMapping(this);
    }

    @Override
    public @Nullable FlowFieldMapping getCopySource() {
        return copySource;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Field getSourceField() {
        return null;
    }

    @Override
    public void setCopySource(Object copySource) {
        NncUtils.requireNull(this.copySource);
        this.copySource = (FlowFieldMapping) copySource;
    }

    @Override
    public FlowFieldMappingParam getParam(SerializeContext serializeContext) {
        return new FlowFieldMappingParam(
                serializeContext.getRef(getter),
                NncUtils.get(setter, serializeContext::getRef)
        );
    }

    @Override
    public Supplier<Value> generateReadCode0(SelfNode selfNode) {
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
        return () -> Values.node(node);
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

    public void setFlows(Method getter, @Nullable Method setter, Type fieldType, CompositeTypeFacade compositeTypeFacade) {
        check(getter, setter);
        this.getter = getter;
        this.setter = setter;
        setReadonly(setter == null);
        getTargetField().setType(fieldType);
//        resetTargetFieldType(compositeTypeFacade);
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
