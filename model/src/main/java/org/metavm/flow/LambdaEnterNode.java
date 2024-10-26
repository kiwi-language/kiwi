package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.LambdaEnterNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Lambda;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@EntityType
public class LambdaEnterNode extends NodeRT implements Callable, LoadAware {

    public static LambdaEnterNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (LambdaEnterNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            LambdaEnterNodeParam param = nodeDTO.getParam();
            var parameters = NncUtils.map(
                    param.parameters(),
                    paramDTO -> new Parameter(paramDTO.tmpId(), paramDTO.name(), paramDTO.code(),
                            TypeParser.parseType(paramDTO.type(), context))
            );
            var returnType = TypeParser.parseType(param.returnType(), context);
            var funcInterface = (ClassType) NncUtils.get(param.functionalInterface(), t -> TypeParser.parseType(t, new ContextTypeDefRepository(context)));
            node = new LambdaEnterNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, parameters, returnType, funcInterface
            );
            node.createSAMImpl();
        }
        return node;
    }

    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private Type returnType;
    private FunctionType functionType;
    private @Nullable ClassType functionalInterface;

    private transient ClassType functionInterfaceImpl;
    private transient  LambdaExitNode exit;

    public LambdaEnterNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope,
                           List<Parameter> parameters,
                           @NotNull Type returnType, @Nullable ClassType functionalInterface) {
        super(tmpId, name, code, functionalInterface != null ? functionalInterface : Types.getFunctionType(parameters, returnType), previous, scope);
        this.returnType = returnType;
        setParameters(parameters, false);
        this.functionalInterface = functionalInterface;
        this.functionType = Types.getFunctionType(parameters, returnType);
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
    protected LambdaEnterNodeParam getParam(SerializeContext serializeContext) {
        return new LambdaEnterNodeParam(
                NncUtils.map(parameters, Parameter::toDTO),
                returnType.toExpression(serializeContext),
                NncUtils.get(functionalInterface, t -> t.toExpression(serializeContext))
        );
    }

    public List<Type> getParameterTypes() {
        return NncUtils.map(parameters, Parameter::getType);
    }

    @Override
    public CallableRef getRef() {
        return null;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void update(List<Parameter> parameters, Type returnType, @Nullable ClassType functionalInterface) {
        setParameters(parameters, false);
        this.returnType = returnType;
        this.functionalInterface = functionalInterface;
        resetType();
    }

    public void setParameters(List<Parameter> parameters) {
        setParameters(parameters, true);
    }

    private void setParameters(List<Parameter> parameters, boolean resetType) {
        NncUtils.forEach(parameters, p -> p.setCallable(this));
        this.parameters.resetChildren(parameters);
        if (resetType)
            resetType();
    }

    @Override
    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    private void resetType() {
        functionType = new FunctionType(getParameterTypes(), returnType);
        setOutputType(functionalInterface != null ? functionalInterface : functionType);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var func = new Lambda(this, frame);
        if (functionInterfaceImpl == null) {
            return NodeExecResult.jump(func, getExit());
        } else {
            var funcImplKlass = functionInterfaceImpl.resolve();
            var funcField = funcImplKlass.getFieldByCode("func");
            return NodeExecResult.jump(
                    ClassInstance.create(Map.of(funcField, func), functionInterfaceImpl).getReference(),
                    getExit()
            );
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("(" + NncUtils.join(parameters, Parameter::getText, ", ") + ")");
        writer.write(": " + returnType.getName());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaEnterNode(this);
    }

    @Override
    public void onLoad() {
        createSAMImpl();
        getExit();
    }

    public void createSAMImpl() {
        functionInterfaceImpl = functionalInterface != null ?
                Types.createFunctionalClass(functionalInterface) : null;
    }

    public LambdaExitNode getExit() {
        if(exit != null)
            return exit;
        int numEntries = 0;
        for(var n = getSuccessor(); n != null; n = n.getSuccessor()) {
            if(n instanceof LambdaEnterNode)
                numEntries++;
            else if(n instanceof LambdaExitNode e) {
                if(numEntries == 0) {
                    exit = e;
                    break;
                }
                numEntries--;
            }
        }
        return Objects.requireNonNull(exit, () -> "Cannot find exit for LambdaEnterNode " + getName());
    }

}
