package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.CapturedTypeKey;
import org.metavm.util.Constants;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO make CapturedType the sub type of or the same type as VariableType
@Entity
public class CapturedType extends Type {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private final Reference variable;

    public CapturedType(CapturedTypeVariable variable) {
        this.variable = variable.getReference();
    }

    public CapturedType(Reference variable) {
        this.variable = variable;
    }

    @Override
    public CapturedTypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new CapturedTypeKey(getTypeDefId.apply(getVariable()));
    }

    @Override
    public Type getUpperBound() {
        return getVariable().getUpperBound();
    }

    @Override
    public Type getLowerBound() {
        return getVariable().getLowerBound();
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
    public String getName() {
        return "CaptureOf" + getVariable().getUncertainType().getName();
    }

    @Override
    public String getTypeDesc() {
        return getVariable().getScope().getScopeName() + "_" + getName();
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
        var variable = getVariable();
        var scope = variable.getScope();
        return scope.getInternalName(current) + ".CaptureOf" + variable.getUncertainType().getInternalName(current) +
                scope.getCapturedTypeVariableIndex(variable);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var variable = getVariable();
        return getTypeDefExpr == null ? "#" + Constants.ID_PREFIX + serializeContext.getStringId(variable)
                : getTypeDefExpr.apply(variable);
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.CAPTURED_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.CAPTURED_TYPE);
        output.writeReference(variable);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    public static CapturedType read(MvInput input) {
        return new CapturedType(input.readReference());
    }

    public UncertainType getUncertainType() {
        return getVariable().getUncertainType();
    }

    @Override
    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        capturedTypes.add(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CapturedType that && variable.equals(that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        action.accept(getVariable());
    }

    @Override
    public boolean isUncertain() {
        return true;
    }

    public CapturedTypeVariable getVariable() {
        return (CapturedTypeVariable) variable.resolveDurable();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCapturedType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitCapturedType(this, s);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(variable);
    }
}
