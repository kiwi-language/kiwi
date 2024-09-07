package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.object.type.rest.dto.VariableTypeKey;
import org.metavm.util.Constants;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@EntityType
public class VariableType extends Type implements IVariableType {

    private final GenericDeclarationRef genericDeclarationRef;
    private final TypeVariable rawVariable;
    private transient TypeVariable resolved;

    public VariableType(@NotNull GenericDeclarationRef genericDeclarationRef, @NotNull TypeVariable rawVariable) {
        super();
        assert rawVariable.getCopySource() == null;
        this.genericDeclarationRef = genericDeclarationRef;
        this.rawVariable = rawVariable;
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
    public TypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
        return new VariableTypeKey(genericDeclarationRef.toGenericDeclarationKey(getTypeDefId), getTypeDefId.apply(rawVariable));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return equals(that);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitVariableType(this, s);
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
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        var prefix = genericDeclarationRef.toExpression(serializeContext, getTypeDefExpr) + "@";
        if(getTypeDefExpr == null)
            return prefix + Constants.ID_PREFIX + serializeContext.getStringId(rawVariable);
        else
            return prefix + getTypeDefExpr.apply(rawVariable);
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.VARIABLE;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.VARIABLE);
        genericDeclarationRef.write(output);
        output.writeId(rawVariable.getId());
    }

    public static VariableType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var genDeclRef = GenericDeclarationRef.read(input, typeDefProvider);
        var rawVariable = (TypeVariable) typeDefProvider.getTypeDef(input.readId());
        return new VariableType(genDeclRef, rawVariable);
    }

    public TypeVariable getVariable() {
        if(resolved != null)
            return resolved;
        return resolved = NncUtils.findRequired(genericDeclarationRef.resolve().getTypeParameters(),
                tv -> tv.getEffectiveTemplate() == rawVariable);
    }

    @Override
    public String getName() {
        return getVariable().getName();
    }

    @Override
    public String getTypeDesc() {
        return getVariable().getTypeDesc();
    }

    @Nullable
    @Override
    public String getCode() {
        return getVariable().getCode();
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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariableType(this);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof VariableType that && rawVariable == that.rawVariable;
    }

    @Override
    public int hashCode() {
        if(getVariable() == null)
            throw new NullPointerException("Variable is null. VariableType: " + System.identityHashCode(this));
        return getVariable().hashCode();
    }

    public TypeVariable getRawVariable() {
        return rawVariable;
    }

    public GenericDeclarationRef getGenericDeclarationRef() {
        return genericDeclarationRef;
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        action.accept(getVariable());
    }
}