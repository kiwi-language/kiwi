package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.type.ClassType;

@EntityType("当前对象节点")
public class SelfNode extends NodeRT<Void> {

    public static ClassType getSelfType(Flow flow, IEntityContext context) {
        var declaringType = flow.getDeclaringType();
        return declaringType.isTemplate() ?
                context.getParameterizedType(declaringType, declaringType.getTypeParameters()) : declaringType;
    }

    public static SelfNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        return new SelfNode(nodeDTO.tmpId(), nodeDTO.name(), getSelfType(scope.getFlow(), context), prev, scope);
    }

    public SelfNode(Long tmpId, String name, ClassType type, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, type, prev, scope);
    }

    @Override
    protected void setParam(Void param, IEntityContext entityContext) {
    }

    @Override
    protected Void getParam(boolean persisting) {
        return null;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        return next(frame.getSelf());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSelfNode(this);
    }
}
