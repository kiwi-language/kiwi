package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.LambdaNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.LambdaInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType("Lambda节点")
public class LambdaNode extends ScopeNode implements Callable, LoadAware {

    public static LambdaNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        LambdaNodeParam param = nodeDTO.getParam();
        var parameters = NncUtils.map(
                param.getParameters(),
                paramDTO -> new Parameter(paramDTO.tmpId(), paramDTO.name(), paramDTO.code(),
                        context.getType(Id.parse(paramDTO.typeId())))
        );
        var parameterTypes = NncUtils.map(parameters, Parameter::getType);
        var returnType = context.getType(Id.parse(param.getReturnTypeId()));
        var funcInterface = NncUtils.get(param.getFunctionalInterfaceId(), id -> context.getClassType(Id.parse(id)));
        var funcType = context.getFunctionTypeContext().getFunctionType(parameterTypes, returnType);
        var node = (LambdaNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            node = new LambdaNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, parameters, returnType,
                    funcType, funcInterface
            );
            node.createSAMImpl(context.getFunctionTypeContext(), context.getGenericContext());
        }
        else
            node.update(parameters, returnType, funcType, funcInterface);
        return node;
    }

    @ChildEntity("参数列表")
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");

    @EntityField("返回类型")
    private Type returnType;

    @EntityField("函数类型")
    private FunctionType functionType;

    @Nullable
    @EntityField("函数接口")
    private ClassType functionalInterface;

    private transient ClassType functionInterfaceImpl;

    public LambdaNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope,
                      List<Parameter> parameters,
                      Type returnType,
                      FunctionType functionType, @Nullable ClassType functionalInterface) {
        super(tmpId, name, code, functionalInterface != null ? functionalInterface : functionType, previous, scope, false);
        checkTypes(parameters, returnType, functionType);
        setParameters(parameters);
        this.returnType = returnType;
        this.functionType = functionType;
        this.functionalInterface = functionalInterface;
    }

    private void checkTypes(List<Parameter> parameters, Type returnType, FunctionType functionType) {
        if (parameters.size() != functionType.getParameterTypes().size())
            throw new InternalException("Parameter size mismatch");
        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).getType().equals(functionType.getParameterTypes().get(i)))
                throw new InternalException("Parameter type mismatch");
        }
        if (!returnType.equals(functionType.getReturnType()))
            throw new InternalException("Return type mismatch");
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

    public Type getReturnType() {
        return returnType;
    }

    @Override
    protected LambdaNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            return new LambdaNodeParam(
                    bodyScope.toDTO(true, serializeContext),
                    NncUtils.map(parameters, Parameter::toDTO),
                    serContext.getId(returnType),
                    NncUtils.get(functionalInterface, serContext::getId)
            );
        }
    }

    public List<Type> getParameterTypes() {
        return NncUtils.map(parameters, Parameter::getType);
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void update(List<Parameter> parameters, Type returnType,
                       FunctionType functionType,
                       @Nullable ClassType functionalInterface) {
        checkTypes(parameters, returnType, functionType);
        setParameters(parameters);
        this.returnType = returnType;
        this.functionalInterface = functionalInterface;
        this.functionType = functionType;
        setOutputType(functionalInterface != null ? functionalInterface : functionType);
    }

    public void setParameters(List<Parameter> parameters) {
        NncUtils.forEach(parameters, p -> p.setCallable(this));
        this.parameters.resetChildren(parameters);
    }

    @Override
    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    @Override
    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var func = new LambdaInstance(this, frame);
        if (functionInterfaceImpl == null) {
            return next(func);
        } else {
            var funcField = functionInterfaceImpl.getFieldByCode("func");
            return next(ClassInstance.create(Map.of(funcField, func), functionInterfaceImpl));
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("(" + NncUtils.join(parameters, Parameter::getText, ", ") + ")");
        writer.write(": " + returnType.getName() + " -> ");
        bodyScope.writeCode(writer);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaNode(this);
    }

    @Override
    public void onLoad(IEntityContext context) {
        createSAMImpl(context.getFunctionTypeContext(), context.getGenericContext());
    }

    public void createSAMImpl(FunctionTypeProvider functionTypeProvider, ParameterizedTypeProvider parameterizedTypeProvider) {
        functionInterfaceImpl = functionalInterface != null ?
                Types.createFunctionalClass(functionalInterface, functionTypeProvider, parameterizedTypeProvider) : null;
    }

}
