package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
import tech.metavm.object.type.rest.dto.VariableTypeKey;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@EntityType("变量类型")
public class VariableType extends Type implements IVariableType {

    private final TypeVariable variable;

    public VariableType(@NotNull TypeVariable variable) {
        super();
        this.variable = variable;
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return Set.of(variable);
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        return equals(that) || super.isAssignableFrom(that);
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, String> getTypeDefId) {
        return new VariableTypeKey(getTypeDefId.apply(variable));
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
        return variable.getUpperBound();
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(variable.getBounds());
    }

    public List<Type> getBounds() {
        return variable.getBounds();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return variable.getInternalName(current);
    }

    @Override
    public VariableType copy() {
        return new VariableType(variable);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        if(getTypeDefExpr == null)
            return "?" + Constants.CONSTANT_ID_PREFIX + serializeContext.getId(variable);
        else
            return getTypeDefExpr.apply(variable);
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.VARIABLE;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.VARIABLE);
        output.writeId(variable.getId());
    }

    public static VariableType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new VariableType((TypeVariable) typeDefProvider.getTypeDef(input.readId()));
    }

    public TypeVariable getVariable() {
        return variable;
    }

    @Override
    public String getName() {
        return variable.getName();
    }

    @Override
    public String getTypeDesc() {
        return variable.getTypeDesc();
    }

    @Nullable
    @Override
    public String getCode() {
        return variable.getCode();
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
        return obj instanceof VariableType that && variable == that.variable;
    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        action.accept(variable);
    }
}