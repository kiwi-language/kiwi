package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.VariableTypeKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
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
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var prefix = genericDeclarationRef.toExpression(serializeContext, getTypeDefExpr) + "@";
        if(getTypeDefExpr == null)
            return prefix + Constants.ID_PREFIX + serializeContext.getStringId(rawVariable);
        else
            return prefix + getTypeDefExpr.apply(rawVariable);
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.VARIABLE_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.VARIABLE_TYPE);
        genericDeclarationRef.write(output);
        output.writeEntityId(rawVariable);
    }

    public static VariableType read(MvInput input) {
        var genDeclRef = GenericDeclarationRef.read(input);
        var rawVariable = input.getTypeVariable(input.readId());
        return new VariableType(genDeclRef, rawVariable);
    }

    public TypeVariable getVariable() {
        if(resolved != null)
            return resolved;
        resolved = NncUtils.find(genericDeclarationRef.resolve().getTypeParameters(),
                tv -> tv.getEffectiveTemplate() == rawVariable);
        if(resolved == null) {
            var method = (Method) genericDeclarationRef.resolve();
            var methodRef = (MethodRef) genericDeclarationRef;
            logger.debug("MethodRef parameterized: {}", methodRef.isParameterized());
            if(methodRef.isParameterized())
                logger.debug("Type argument equals type parameter: {}", methodRef.getTypeArguments().get(0)
                        .equals(methodRef.getRawFlow().getTypeParameters().get(0).getType()));
            logger.debug("Type parameter count: {}", method.getTypeParameters().size());
            for (TypeVariable typeParameter : method.getTypeParameters()) {
                logger.debug("Type parameter {}, equals: {}", typeParameter.getName(), typeParameter == rawVariable);
            }
            logger.debug("Parameterized: {}", method.isParameterized());
            throw new RuntimeException("Cannot find type variable " + rawVariable.getName() + " in generic declaration" + method.getDeclaringType().getQualifiedName()
                    + "." + method.getName());
        }
        return resolved;
    }

    @Override
    public String getName() {
        return getVariable().getName();
    }

    @Override
    public String getTypeDesc() {
        return rawVariable.getName();
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
        return obj instanceof VariableType that
                && genericDeclarationRef.equals(that.genericDeclarationRef)
                && rawVariable == that.rawVariable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(genericDeclarationRef, rawVariable);
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

    @Override
    public int getPrecedence() {
        return 0;
    }
}