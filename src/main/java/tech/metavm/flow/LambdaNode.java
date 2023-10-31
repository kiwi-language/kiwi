package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.LambdaNodeParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.LambdaInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.FunctionType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeUtil;
import tech.metavm.util.ChildArray;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType("Lambda节点")
public class LambdaNode extends ScopeNode<LambdaNodeParamDTO> implements Callable {

    public static LambdaNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        LambdaNodeParamDTO param = nodeDTO.getParam();
        var parameters = NncUtils.map(
                param.getParameters(),
                paramDTO -> new Parameter(paramDTO.tmpId(), paramDTO.name(), paramDTO.code(),
                        context.getType(paramDTO.typeRef()))
        );
        var parameterTypes = NncUtils.map(parameters, Parameter::getType);
        var returnType = context.getType(param.getReturnTypeRef());
        var funcInterface = NncUtils.get(param.getFunctionalInterfaceRef(), context::getClassType);
        var funcType = context.getFunctionTypeContext().get(parameterTypes, returnType);
        return new LambdaNode(
                nodeDTO.tmpId(), nodeDTO.name(), prev, scope, parameters, returnType,
                funcType, funcInterface
        );
    }

    @ChildEntity("参数列表")
    private final ChildArray<Parameter> parameters = new ChildArray<>(Parameter.class);

    @EntityField("返回类型")
    private Type returnType;

    @EntityField("函数类型")
    private FunctionType functionType;

    @Nullable
    @EntityField("函数接口")
    private ClassType functionalInterface;

    public LambdaNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope,
                      List<Parameter> parameters, Type returnType,
                      FunctionType functionType, @Nullable ClassType functionalInterface) {
        super(tmpId, name, functionalInterface != null ? functionalInterface : functionType, previous, scope, false);
        this.parameters.addChildren(parameters);
        this.returnType = returnType;
        this.functionType = functionType;
        this.functionalInterface = functionalInterface;
    }

    @Override
    @NotNull
    public Type getType() {
        return NncUtils.requireNonNull(super.getType());
    }


    @Nullable
    public ClassType getFunctionalInterface() {
        return functionalInterface;
    }

    public List<Parameter> getParameters() {
        return NncUtils.listOf(parameters);
    }

    @Override
    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    protected LambdaNodeParamDTO getParam(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            return new LambdaNodeParamDTO(
                    bodyScope.toDTO(true),
                    NncUtils.map(parameters, Parameter::toDTO),
                    context.getRef(returnType),
                    NncUtils.get(functionalInterface, context::getRef)
            );
        }
    }

    @Override
    protected void setParam(LambdaNodeParamDTO param, IEntityContext context) {
        if (param.getParameters() != null) {
            parameters.resetChildren(
                    NncUtils.map(
                            param.getParameters(),
                            paramDTO -> new Parameter(paramDTO.tmpId(), paramDTO.name(), paramDTO.code(),
                                    context.getType(paramDTO.typeRef()))
                    )
            );
        }
        if (param.getReturnTypeRef() != null) {
            this.returnType = context.getType(param.getReturnTypeRef());
        }
        setOutputType(context.getFunctionTypeContext().get(getParameterTypes(), returnType));
    }

    public List<Type> getParameterTypes() {
        return NncUtils.map(parameters, Parameter::getType);
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters.resetChildren(parameters);
    }

    @Override
    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    @Override
    public void execute(MetaFrame frame) {
        var func = new LambdaInstance(this, frame);
        if (functionalInterface == null) {
            frame.setResult(func);
        } else {
            var funcClass = TypeUtil.createFunctionalClass(
                    functionalInterface,
                    frame.getStack().getContext().getEntityContext());
            var funcField = funcClass.getFieldByCodeRequired("func");
            frame.setResult(new ClassInstance(Map.of(funcField, func), funcClass));
        }
    }

}
