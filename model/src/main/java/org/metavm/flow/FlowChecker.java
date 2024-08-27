package org.metavm.flow;

import org.metavm.entity.Element;
import org.metavm.entity.StructuralVisitor;
import org.metavm.object.type.Klass;
import org.metavm.object.type.MetadataState;
import org.metavm.util.DebugEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowChecker extends StructuralVisitor<Boolean> {

    public static final Logger logger = LoggerFactory.getLogger(FlowChecker.class);

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
                logger.debug("Error in node {}: {}",
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
