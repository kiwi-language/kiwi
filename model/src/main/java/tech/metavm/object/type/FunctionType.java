package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.FunctionTypeKey;
import tech.metavm.object.type.rest.dto.FunctionTypeParam;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType("函数类型")
public class FunctionType extends CompositeType {

    public static final IndexDef<FunctionType> KEY_IDX = IndexDef.createUnique(
            FunctionType.class, "key");

    public static final IndexDef<FunctionType> PARAMETER_TYPE_KEY = IndexDef.create(
            FunctionType.class, "parameterTypes"
    );

    public static final IndexDef<FunctionType> RETURN_TYPE_KEY = IndexDef.create(
            FunctionType.class, "returnType"
    );


    @EntityField("返回类型")
    private Type returnType;

    @ChildEntity("参数类型列表")
    private final ReadWriteArray<Type> parameterTypes = addChild(new ReadWriteArray<>(Type.class), "parameterTypes");

    public FunctionType(Long tmpId, List<Type> parameterTypes, Type returnType) {
        super(getName(parameterTypes, returnType), getCode(parameterTypes, returnType), false, true, TypeCategory.FUNCTION);
        setTmpId(tmpId);
        this.parameterTypes.addAll(parameterTypes);
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
    public TypeKey getTypeKey() {
        return new FunctionTypeKey(NncUtils.map(parameterTypes, Type::getStringId), returnType.getStringId());
    }

    @Override
    protected boolean isAssignableFrom0(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        if (that instanceof FunctionType functionType) {
            if (parameterTypes.size() == functionType.parameterTypes.size()) {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    if (!parameterTypes.get(i).isAssignableFrom(((FunctionType) that).parameterTypes.get(i), typeMapping)) {
                        return false;
                    }
                }
                return functionType.returnType.isAssignableFrom(returnType, typeMapping);
            }
        }
        return false;
    }

    @Override
    public boolean equals(Type that, @Nullable Map<TypeVariable, ? extends Type> mapping) {
        if(that instanceof FunctionType thatFuncType) {
            if(thatFuncType.parameterTypes.size() != parameterTypes.size())
                return false;
            return returnType.equals(thatFuncType.returnType, mapping)
                    && NncUtils.biAllMatch(parameterTypes, thatFuncType.parameterTypes, (t1, t2) -> t1.equals(t2, mapping));
        }
        return false;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setParameterTypes(List<Type> parameterTypes) {
        this.parameterTypes.clear();
        this.parameterTypes.addAll(parameterTypes);
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
                    NncUtils.map(parameterTypes, serContext::getId),
                    serContext.getId(returnType)
            );
        }
    }

    @Override
    public String getTypeDesc() {
        return "(" + NncUtils.join(parameterTypes, Type::getTypeDesc) + ")" + "->" + returnType.getTypeDesc();
    }

    @Override
    public List<Type> getComponentTypes() {
        return NncUtils.append(getParameterTypes(), returnType);
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return "(" + NncUtils.join(parameterTypes, object -> context.getModelName(object, this)) + ")"
                + "->" + context.getModelName(returnType, this);
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
}
