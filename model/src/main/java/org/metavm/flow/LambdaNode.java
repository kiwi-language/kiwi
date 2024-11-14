package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.LoadAware;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class LambdaNode extends Node implements LoadAware {

    private final Lambda lambda;
    private final @Nullable ClassType functionalInterface;

    private transient ClassType functionInterfaceImpl;

    public LambdaNode(String name, Node previous, Code code,
                      @NotNull Lambda lambda, @Nullable ClassType functionalInterface) {
        super(name, functionalInterface != null ? functionalInterface : lambda.getFunctionType(), previous, code);
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
    public void writeContent(CodeWriter writer) {
        writer.write("(" + NncUtils.join(lambda.getParameters(), Parameter::getText, ", ") + ")");
        writer.write(": " + lambda.getReturnType().getName());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LAMBDA);
        output.writeConstant(lambda.getRef());
        if(functionalInterface != null) {
            output.writeBoolean(true);
            output.writeConstant(functionalInterface);
        }
        else
            output.writeBoolean(false);
    }

    @Override
    public int getLength() {
        return functionInterfaceImpl == null ? 4 : 6;
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
