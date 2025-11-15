package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.FunctionTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public class FunctionType extends CompositeType {

    private final List<Type> parameterTypes;
    @Getter
    @Setter
    private Type returnType;

    public FunctionType(List<Type> parameterTypes, @NotNull Type returnType) {
        super();
        this.parameterTypes = new ArrayList<>(parameterTypes);
        this.returnType = returnType;
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new FunctionTypeKey(Utils.map(parameterTypes, type -> type.toTypeKey(getTypeDefId)), returnType.toTypeKey(getTypeDefId));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof FunctionType thatFuncType) {
            if (parameterTypes.size() == thatFuncType.parameterTypes.size()) {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    if (!thatFuncType.parameterTypes.get(i).isAssignableFrom(parameterTypes.get(i))) {
                        return false;
                    }
                }
                return returnType.isAssignableFrom(thatFuncType.returnType);
            }
        }
        return false;
    }

    public List<Type> getParameterTypes() {
        return Collections.unmodifiableList(parameterTypes);
    }

    @Override
    public String getName() {
        return "(" + Utils.join(parameterTypes, Type::getName) + ")->" + returnType.getName();
    }

    @Override
    public String getTypeDesc() {
        return "(" + Utils.join(parameterTypes, Type::getTypeDesc) + ")" + "->" + returnType.getTypeDesc();
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.FUNCTION;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public List<Type> getComponentTypes() {
        return Utils.append(getParameterTypes(), returnType);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return "(" + Utils.join(parameterTypes, type -> type.getInternalName(current)) + ")"
                + "->" + returnType.getInternalName(current);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return "(" + Utils.join(parameterTypes, type -> type.toExpression(serializeContext, getTypeDefExpr)) + ")" + "->" + returnType.toExpression(serializeContext, getTypeDefExpr);
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.FUNCTION_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FUNCTION_TYPE);
        output.writeList(parameterTypes, t -> t.write(output));
        returnType.write(output);
    }

    @Override
    public int getPrecedence() {
        return 4;
    }

    public static FunctionType read(MvInput input) {
        return new FunctionType(input.readList(input::readType), input.readType());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FunctionType that && parameterTypes.equals(that.parameterTypes) && returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, returnType);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitFunctionType(this, s);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        parameterTypes.forEach(arg -> arg.accept(visitor));
        returnType.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        for (var parameterTypes_ : parameterTypes) parameterTypes_.forEachReference(action);
        returnType.forEachReference(action);
    }
}
