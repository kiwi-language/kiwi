package tech.metavm.flow;

import tech.metavm.entity.Element;
import tech.metavm.expression.StructuralVisitor;
import tech.metavm.object.meta.ClassType;

public class FlowChecker extends StructuralVisitor<Boolean> {

    private Flow flow;

    @Override
    public Boolean visitClassType(ClassType type) {
        super.visitClassType(type);
        return type.isError();
    }

    @Override
    public Boolean visitFlow(Flow flow) {
        this.flow = flow;
        flow.getDeclaringType().clearElementErrors(flow);
        flow.setError(false);
        super.visitFlow(flow);
        if (flow.isError()) {
            flow.getDeclaringType().addError(
                    flow,
                    ErrorLevel.ERROR,
                    String.format("流程'%s'配置错误", flow.getName())
            );
        }
        this.flow = null;
        return flow.isError();
    }

    @Override
    public Boolean visitNode(NodeRT<?> node) {
        node.check();
        if (node.getError() != null)
            flow.setError(true);
        super.visitNode(node);
        return node.getError() != null;
    }

    @Override
    public Boolean defaultValue(Element element) {
        return true;
    }
}
