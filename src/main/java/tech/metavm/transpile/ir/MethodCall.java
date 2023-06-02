package tech.metavm.transpile.ir;

import tech.metavm.transpile.ir.gen2.Graph;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MethodCall extends IRExpressionBase {
    @Nullable
    private final IRExpression instance;
    private final IRMethod method;
    private final List<IRType> typeArguments;
    private final List<IRExpression> arguments;
    private final Graph graph;

    public MethodCall(@Nullable IRExpression instance,
                      List<IRType> typeArguments, IRMethod method,
                      List<IRExpression> arguments, Graph graph) {
        this.instance = instance;
        this.method = method;
        this.typeArguments = new ArrayList<>(typeArguments);
        this.arguments = new ArrayList<>(arguments);
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

    @Nullable
    public IRExpression getInstance() {
        return instance;
    }


    public IRMethod getMethod() {
        return method;
    }

    public List<IRType> getTypeArguments() {
        return typeArguments;
    }

    public List<IRExpression> getArguments() {
        return arguments;
    }

    @Override
    public IRType type() {
        return method.returnType();
    }
}
