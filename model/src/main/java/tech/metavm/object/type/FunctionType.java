package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.FunctionTypeKey;
import tech.metavm.object.type.rest.dto.FunctionTypeParam;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType
public class FunctionType extends CompositeType {

    private Type returnType;
    private final ValueArray<Type> parameterTypes;

    public FunctionType(List<Type> parameterTypes, @NotNull Type returnType) {
        super(getName(parameterTypes, returnType), getCode(parameterTypes, returnType), false, true, TypeCategory.FUNCTION);
        this.parameterTypes = new ValueArray<>(Type.class, parameterTypes);
        this.returnType = returnType;
    }

    private static String getName(List<Type> parameterTypes, Type returnType) {
        return "(" + NncUtils.join(parameterTypes, Type::getName) + ")->" + returnType.getName();
    }

    private static @Nullable String getCode(List<Type> parameterTypes, Type returnType) {
        if(returnType.getCode() != null && NncUtils.allMatch(parameterTypes, t -> t.getCode() != null))
            return "(" + NncUtils.join(parameterTypes, Type::getCode) + ")->" + returnType.getCode();
        else
            return null;
    }


    @Override
    public TypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
        return new FunctionTypeKey(NncUtils.map(parameterTypes, type -> type.toTypeKey(getTypeDefId)), returnType.toTypeKey(getTypeDefId));
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

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitFunctionType(this, s);
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes.toList();
    }

    @Override
    protected FunctionTypeParam getParamInternal() {
        try (var serContext = SerializeContext.enter()) {
            return new FunctionTypeParam(
                    NncUtils.map(parameterTypes, serContext::getStringId),
                    serContext.getStringId(returnType)
            );
        }
    }

    @Override
    public String getName() {
        return "(" + NncUtils.join(parameterTypes, Type::getName) + ")->" + returnType.getName();
    }

    @Override
    public String getTypeDesc() {
        return "(" + NncUtils.join(parameterTypes, Type::getTypeDesc) + ")" + "->" + returnType.getTypeDesc();
    }

    @Nullable
    @Override
    public String getCode() {
        if(returnType.getCode() == null || NncUtils.anyMatch(parameterTypes, t -> t.getCode() == null))
            return null;
        return "(" + NncUtils.join(parameterTypes, Type::getCode) + ")->" + returnType.getCode();
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
        return NncUtils.append(getParameterTypes(), returnType);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return "(" + NncUtils.join(parameterTypes, type -> type.getInternalName(current)) + ")"
                + "->" + returnType.getInternalName(current);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionType(this);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        return "(" + NncUtils.join(parameterTypes, type -> type.toExpression(serializeContext, getTypeDefExpr)) + ")" + "->" + returnType.toExpression(serializeContext, getTypeDefExpr);
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.FUNCTION;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.FUNCTION);
        output.writeInt(parameterTypes.size());
        parameterTypes.forEach(t -> t.write(output));
        returnType.write(output);
    }

    public static FunctionType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var numParamTypes = input.readInt();
        var paramTypes = new ArrayList<Type>(numParamTypes);
        for (int i = 0; i < numParamTypes; i++)
            paramTypes.add(Type.readType(input, typeDefProvider));
        return new FunctionType(paramTypes, Type.readType(input, typeDefProvider));
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof FunctionType that && parameterTypes.equals(that.parameterTypes) && returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, returnType);
    }
}
