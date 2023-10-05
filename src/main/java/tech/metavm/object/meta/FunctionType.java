package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.meta.rest.dto.FunctionTypeParamDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@EntityType("函数类型")
public class FunctionType extends Type {

    @EntityField("返回类型")
    private Type returnType;

    @EntityField("参数类型列表")
    private final Table<Type> parameterTypes = new Table<>(Type.class, false);

    public FunctionType(List<Type> parameterTypes, Type returnType) {
        super(createName(parameterTypes, returnType), true, true, TypeCategory.FUNCTION);
    }

    private static String createName(List<Type> parameterTypes, Type returnType) {
        return "(" + NncUtils.join(parameterTypes, Type::getName) + "):" + returnType.getName();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if(that instanceof FunctionType functionType) {
            if(parameterTypes.size() == functionType.parameterTypes.size()) {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    if(!parameterTypes.get(i).isAssignableFrom(((FunctionType) that).parameterTypes.get(i))) {
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
        return Collections.unmodifiableList(parameterTypes);
    }

    @Override
    protected FunctionTypeParamDTO getParam() {
        try(var context = SerializeContext.enter()) {
            return new FunctionTypeParamDTO(
                    NncUtils.map(parameterTypes, context::getRef),
                    context.getRef(returnType)
            );
        }
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "(" + NncUtils.join(parameterTypes, paramType -> paramType.getCanonicalName(getJavaType)) + ")"
                + ":" + returnType.getCanonicalName(getJavaType);
    }
}
