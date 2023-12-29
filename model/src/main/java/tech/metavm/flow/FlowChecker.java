package tech.metavm.flow;

import tech.metavm.entity.Element;
import tech.metavm.entity.StructuralVisitor;
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
    public Boolean visitFunction(Function function) {
        this.flow = function;
        function.setState(MetadataState.READY);
        super.visitFunction(function);
        this.flow = null;
        return function.isError();
    }

    @Override
    public Boolean visitMethod(Method method) {
        this.flow = method;
        method.getDeclaringType().clearElementErrors(method);
        method.setState(MetadataState.READY);
        super.visitMethod(method);
        if (method.isError()) {
            method.getDeclaringType().addError(
                    method,
                    ErrorLevel.ERROR,
                    String.format("方法'%s'配置错误", method.getName())
            );
        }
        this.flow = null;
        return method.isError();
    }

    @Override
    public Boolean visitNode(NodeRT node) {
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
