package org.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.CapturedTypeKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.util.Constants;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO make CapturedType the sub type of or the same type as VariableType
@EntityType
public class CapturedType extends Type {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final CapturedTypeVariable variable;

    public CapturedType(CapturedTypeVariable variable) {
        super(
        );
        this.variable = variable;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCapturedType(this);
    }

    @Override
    public CapturedTypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new CapturedTypeKey(getTypeDefId.apply(variable));
    }

    @Override
    public Type getUpperBound() {
        return variable.getUpperBound();
    }

    @Override
    public Type getLowerBound() {
        return variable.getLowerBound();
    }

    @Override
    public boolean isCaptured() {
        return true;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return this == that;
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitCapturedType(this, s);
    }

    @Override
    public String getName() {
        return "CaptureOf" + variable.getUncertainType().getName();
    }

    @Override
    public String getTypeDesc() {
        return variable.getScope().getScopeName() + "_" + getName();
    }

    @Nullable
    @Override
    public String getCode() {
        return null;
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.CAPTURED;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        var scope = variable.getScope();
        return scope.getInternalName(current) + ".CaptureOf" + variable.getUncertainType().getInternalName(current) +
                scope.getCapturedTypeVariableIndex(variable);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return getTypeDefExpr == null ? "#" + Constants.ID_PREFIX + serializeContext.getStringId(variable)
                : getTypeDefExpr.apply(variable);
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.CAPTURED;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.CAPTURED);
        output.writeId(variable.getId());
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    public static CapturedType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new CapturedType((CapturedTypeVariable) typeDefProvider.getTypeDef(input.readId()));
    }

    public UncertainType getUncertainType() {
        return variable.getUncertainType();
    }

    @Override
    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        capturedTypes.add(this);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof CapturedType that && variable == that.variable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        action.accept(variable);
    }

    @Override
    public boolean isUncertain() {
        return true;
    }
}
