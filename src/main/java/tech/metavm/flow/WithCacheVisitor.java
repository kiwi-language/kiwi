package tech.metavm.flow;

import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.StaticFieldExpression;
import tech.metavm.expression.VoidStructuralVisitor;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.CompositeType;
import tech.metavm.object.type.Type;

public class WithCacheVisitor extends VoidStructuralVisitor {

    private final IEntityContext entityContext;

    public WithCacheVisitor(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    private void setLoadTypeWithCache(Type type) {
        entityContext.setLoadWithCache(type);
        if (type instanceof ClassType classType && classType.isParameterized())
            classType.getTypeArguments().forEach(this::setLoadTypeWithCache);
        else if (type instanceof CompositeType compositeType)
            compositeType.getComponentTypes().forEach(this::setLoadTypeWithCache);
    }

    @Override
    public Void visitFlow(Flow flow) {
        setLoadTypeWithCache(flow.getReturnType());
        entityContext.setLoadWithCache(flow.getRootScope());
        EntityUtils.ensureProxyInitialized(flow.getRootScope());
        return super.visitFlow(flow);
    }

    @Override
    public Void visitParameter(Parameter parameter) {
        setLoadTypeWithCache(parameter.getType());
        return super.visitParameter(parameter);
    }

    @Override
    public Void visitNode(NodeRT<?> node) {
        if (node.getType() != null)
            setLoadTypeWithCache(node.getType());
        return super.visitNode(node);
    }

    @Override
    public Void visitExpression(Expression expression) {
        setLoadTypeWithCache(expression.getType());
        return super.visitExpression(expression);
    }

    @Override
    public Void visitStaticFieldExpression(StaticFieldExpression expression) {
        setLoadTypeWithCache(expression.getField().getDeclaringType());
        return super.visitStaticFieldExpression(expression);
    }
}
