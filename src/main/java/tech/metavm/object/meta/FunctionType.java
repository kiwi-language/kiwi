package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.meta.rest.dto.FunctionTypeKey;
import tech.metavm.object.meta.rest.dto.FunctionTypeParam;
import tech.metavm.object.meta.rest.dto.TypeKey;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.function.Function;

@EntityType("函数类型")
public class FunctionType extends CompositeType {

    public static final IndexDef<FunctionType> KEY_IDX = IndexDef.uniqueKey(
            FunctionType.class, "key");

    public static final IndexDef<FunctionType> PARAMETER_TYPE_KEY = IndexDef.normalKey(
            FunctionType.class, "parameterTypes"
    );

    public static final IndexDef<FunctionType> RETURN_TYPE_KEY = IndexDef.normalKey(
            FunctionType.class, "returnType"
    );


    @EntityField("返回类型")
    private Type returnType;

    @ChildEntity("参数类型列表")
    private final ReadWriteArray<Type> parameterTypes = addChild(new ReadWriteArray<>(Type.class), "parameterTypes");

    public FunctionType(Long tmpId, List<Type> parameterTypes, Type returnType) {
        super(createName(parameterTypes, returnType), false, true, TypeCategory.FUNCTION);
        setTmpId(tmpId);
        this.parameterTypes.addAll(parameterTypes);
        this.returnType = returnType;
    }

    private static String createName(List<Type> parameterTypes, Type returnType) {
        return "(" + NncUtils.join(parameterTypes, Type::getName) + ")->" + returnType.getName();
    }

    @Override
    public TypeKey getTypeKey() {
        return new FunctionTypeKey(NncUtils.map(parameterTypes, Type::getRef), returnType.getRef());
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof FunctionType functionType) {
            if (parameterTypes.size() == functionType.parameterTypes.size()) {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    if (!parameterTypes.get(i).isAssignableFrom(((FunctionType) that).parameterTypes.get(i))) {
                        return false;
                    }
                }
                return functionType.returnType.isAssignableFrom(returnType);
            }
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
        try (var context = SerializeContext.enter()) {
            return new FunctionTypeParam(
                    NncUtils.map(parameterTypes, context::getRef),
                    context.getRef(returnType)
            );
        }
    }

    @Override
    public List<Type> getComponentTypes() {
        return NncUtils.append(getParameterTypes(), returnType);
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "(" + NncUtils.join(parameterTypes, paramType -> paramType.getCanonicalName(getJavaType)) + ")"
                + "->" + returnType.getCanonicalName(getJavaType);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionType(this);
    }
}
