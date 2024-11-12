package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.LoadAware;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LambdaNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LambdaValue;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Map;

@EntityType
public class LambdaNode extends NodeRT implements LoadAware {

    public static LambdaNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (LambdaNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            LambdaNodeParam param = nodeDTO.getParam();
            var lambda = context.getEntity(Lambda.class, param.lambdaId());
            var funcInterface = (ClassType) NncUtils.get(param.functionalInterface(), t -> TypeParser.parseType(t, new ContextTypeDefRepository(context)));
            node = new LambdaNode(
                    nodeDTO.tmpId(), nodeDTO.name(), prev, scope, lambda, funcInterface
            );
            node.createSAMImpl();
        }
        return node;
    }

    private final Lambda lambda;
    private final @Nullable ClassType functionalInterface;

    private transient ClassType functionInterfaceImpl;

    public LambdaNode(Long tmpId, String name, NodeRT previous, ScopeRT scope,
                      @NotNull Lambda lambda, @Nullable ClassType functionalInterface) {
        super(tmpId, name, functionalInterface != null ? functionalInterface : lambda.getFunctionType(), previous, scope);
        this.functionalInterface = functionalInterface;
        this.lambda = lambda;
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

    @Override
    protected LambdaNodeParam getParam(SerializeContext serializeContext) {
        return new LambdaNodeParam(
                serializeContext.getStringId(lambda),
                NncUtils.get(functionalInterface, t -> t.toExpression(serializeContext))
        );
    }

    @Override
    public int execute(MetaFrame frame) {
        var func = new LambdaValue(lambda, frame);
        if (functionInterfaceImpl == null) {
            frame.push(func);
        } else {
            var funcImplKlass = functionInterfaceImpl.resolve();
            var funcField = funcImplKlass.getFieldByCode("func");
            frame.push(ClassInstance.create(Map.of(funcField, func), functionInterfaceImpl).getReference());
        }
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("(" + NncUtils.join(lambda.getParameters(), Parameter::getText, ", ") + ")");
        writer.write(": " + lambda.getReturnType().getName());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaEnterNode(this);
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
