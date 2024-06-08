package tech.metavm.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.Element;
import tech.metavm.entity.StructuralVisitor;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.MetadataState;
import tech.metavm.util.DebugEnv;

public class FlowChecker extends StructuralVisitor<Boolean> {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private Flow flow;

    @Override
    public Boolean visitKlass(Klass klass) {
        super.visitKlass(klass);
        return klass.isError();
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
                    String.format("Error in method '%s'", method.getName())
            );
        }
        this.flow = null;
        return method.isError();
    }

    @Override
    public Boolean visitNode(NodeRT node) {
        node.check();
        if (node.getError() != null) {
            flow.setState(MetadataState.ERROR);
            if(DebugEnv.debugging)
                debugLogger.error("Error in node {}: {}",
                        node.getFlow().getQualifiedName() + "." + node.getName(),
                        node.getError());
        }
        super.visitNode(node);
        return node.getError() != null;
    }

    @Override
    public Boolean defaultValue(Element element) {
        return true;
    }
}
