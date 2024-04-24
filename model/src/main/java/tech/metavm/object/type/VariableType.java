package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.VariableTypeKey;
import tech.metavm.object.type.rest.dto.TypeVariableParam;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.*;

@EntityType("变量类型")
public class VariableType extends Type implements IVariableType {

    private final TypeVariable variable;

    public VariableType(TypeVariable variable) {
        super(variable.getName(), variable.getCode(), false, false, TypeCategory.VARIABLE);
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
    public TypeKey toTypeKey() {
        return new VariableTypeKey(variable.getStringId());
    }

    @Override
    public boolean isValidGlobalKey() {
        return false;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return equals(that);
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
    protected TypeVariableParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            return new TypeVariableParam(serContext.getId(variable));
        }
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
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
    public String toTypeExpression(SerializeContext serializeContext) {
        return Constants.CONSTANT_ID_PREFIX + serializeContext.getId(this);
    }

    @Override
    public void write0(InstanceOutput output) {
        output.writeId(variable.getId());
    }

    public static VariableType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new VariableType((TypeVariable) typeDefProvider.getTypeDef(input.readId()));
    }

    public TypeVariable getVariable() {
        return variable;
    }

    @Override
    public String getTypeDesc() {
        return variable.getTypeDesc();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariableType(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VariableType that && variable == that.variable;
    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }
}