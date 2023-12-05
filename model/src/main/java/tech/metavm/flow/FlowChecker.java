package tech.metavm.flow;

import tech.metavm.entity.Element;
import tech.metavm.expression.StructuralVisitor;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.MetadataState;

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
        flow.setState(MetadataState.READY);
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
            flow.setState(MetadataState.ERROR);
        super.visitNode(node);
        return node.getError() != null;
    }

    @Override
    public Boolean defaultValue(Element element) {
        return true;
    }
}
