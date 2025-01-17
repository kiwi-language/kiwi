package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.VariableTypeKey;
import org.metavm.util.Constants;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public class VariableType extends Type implements IVariableType {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final Reference variableReference;
//    private TypeVariable variable;

    public VariableType(@NotNull TypeVariable variable) {
        super();
        this.variableReference = variable.getReference();
//        this.variable = variable;
    }

    public VariableType(Reference variableReference) {
        this.variableReference = variableReference;
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return Set.of(getVariable());
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        return equals(that) || super.isAssignableFrom(that);
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new VariableTypeKey(getTypeDefId.apply(getVariable()));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return equals(that);
    }

    @Override
    public Type getUpperBound() {
        return getVariable().getUpperBound();
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(getVariable().getBounds());
    }

    public List<Type> getBounds() {
        return getVariable().getBounds();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return getVariable().getInternalName(current);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var prefix = "@";
        if(getTypeDefExpr == null)
            return prefix + Constants.ID_PREFIX + serializeContext.getStringId(getVariable());
        else
            return prefix + getTypeDefExpr.apply(getVariable());
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.VARIABLE_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.VARIABLE_TYPE);
        output.writeReference(variableReference);
    }

    public static VariableType read(MvInput input) {
        return new VariableType(input.readReference());
    }

    public TypeVariable getVariable() {
        return (TypeVariable) variableReference.get();
    }

    @Override
    public String getName() {
        return getVariable().getName();
    }

    @Override
    public String getTypeDesc() {
        return getVariable().getQualifiedName();
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.VARIABLE;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VariableType that && variableReference.equals(that.variableReference);
    }

    @Override
    public int hashCode() {
        return variableReference.hashCode();
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        action.accept(getVariable());
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariableType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitVariableType(this, s);
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
        action.accept(variableReference);
    }
}