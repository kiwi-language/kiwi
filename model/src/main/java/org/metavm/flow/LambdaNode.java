package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.LambdaNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LambdaInstance;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType
public class LambdaNode extends ScopeNode implements Callable, LoadAware {

    public static LambdaNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        LambdaNodeParam param = nodeDTO.getParam();
        var parameters = NncUtils.map(
                param.getParameters(),
                paramDTO -> new Parameter(paramDTO.tmpId(), paramDTO.name(), paramDTO.code(),
                        TypeParser.parseType(paramDTO.type(), context))
        );
        var returnType = TypeParser.parseType(param.getReturnType(), context);
        var funcInterface = (ClassType) NncUtils.get(param.getFunctionalInterface(), t -> TypeParser.parseType(t, new ContextTypeDefRepository(context)));
        var node = (LambdaNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            node = new LambdaNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, parameters, returnType, funcInterface
            );
            node.createSAMImpl();
        } else
            node.update(parameters, returnType, funcInterface);
        return node;
    }

    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private Type returnType;
    private FunctionType functionType;
    private @Nullable ClassType functionalInterface;

    private transient ClassType functionInterfaceImpl;

    public LambdaNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope,
                      List<Parameter> parameters,
                      @NotNull Type returnType, @Nullable ClassType functionalInterface) {
        super(tmpId, name, code, functionalInterface != null ? functionalInterface : Types.getFunctionType(parameters, returnType), previous, scope, false);
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
    protected LambdaNodeParam getParam(SerializeContext serializeContext) {
        return new LambdaNodeParam(
                bodyScope.toDTO(true, serializeContext),
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
        var func = new LambdaInstance(this, frame);
        if (functionInterfaceImpl == null) {
            return next(func);
        } else {
            var funcImplKlass = functionInterfaceImpl.resolve();
            var funcField = funcImplKlass.getFieldByCode("func");
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
    public void onLoad() {
        createSAMImpl();
    }

    public void createSAMImpl() {
        functionInterfaceImpl = functionalInterface != null ?
                Types.createFunctionalClass(functionalInterface) : null;
    }

}
