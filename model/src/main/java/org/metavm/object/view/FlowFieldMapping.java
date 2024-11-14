package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.flow.*;
import org.metavm.object.type.Field;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Type;
import org.metavm.object.view.rest.dto.FlowFieldMappingParam;
import org.metavm.util.BusinessException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@EntityType
public class FlowFieldMapping extends FieldMapping implements LocalKey, GenericElement {

    private MethodRef getterRef;
    @Nullable
    private MethodRef setterRef;
    @CopyIgnore
    @Nullable
    private FlowFieldMapping copySource;

    public FlowFieldMapping(Long tmpId,
                            FieldsObjectMapping containingMapping,
                            @Nullable NestedMapping nestedMapping,
                            FieldRef targetFieldRef,
                            @NotNull Method getter,
                            @Nullable Method setter,
                            @Nullable FlowFieldMapping copySource) {
        super(tmpId, targetFieldRef, containingMapping, nestedMapping);
        check(getter, setter);
        this.getterRef = getter.getRef();
        this.setterRef = NncUtils.get(setter, Method::getRef);
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
                getterRef.toDTO(serializeContext),
                NncUtils.get(setterRef, r -> r.toDTO(serializeContext))
        );
    }

    @Override
    public Type generateReadCode0(Code code) {
        var getter = getterRef.resolve();
        Nodes.this_(code);
        Nodes.methodCall(getter, code);
        return getter.getReturnType();
    }

    @Override
    protected void generateWriteCode0(Supplier<Type> getFieldValue, Code code) {
        Nodes.this_(code);
        getFieldValue.get();
        Nodes.methodCall(getSetter(), code);
    }

    @Override
    protected Type getTargetFieldType() {
        return getGetter().getReturnType();
    }

    public void setFlows(@NotNull Method getter, @Nullable Method setter, Type fieldType) {
        check(getter, setter);
        this.getterRef = getter.getRef();
        this.setterRef = NncUtils.get(setter, Method::getRef);
        setReadonly(setterRef == null);
        getTargetField().setType(fieldType);
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
        return getterRef.resolve();
    }

    @Nullable
    public Method getSetter() {
        return NncUtils.get(setterRef, MethodRef::resolve);
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getGetter().getName();
    }

}
