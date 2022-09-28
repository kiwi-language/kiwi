package tech.metavm.object.instance.query;

import tech.metavm.flow.FlowRT;
import tech.metavm.flow.NodeRT;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;

import java.util.Map;

public class FlowParsingContext implements ParsingContext {

    private final FlowRT flow;

    public FlowParsingContext(FlowRT flow) {
        this.flow = flow;
    }

    @Override
    public Expression parseField(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if(idx == -1) {
            invalidExpression(fieldPath);
        }
        String nodeName = fieldPath.substring(0, idx);
        String subPath = fieldPath.substring(idx + 1);
        NodeRT<?> node = getNode(nodeName);
        if(node == null || node.getOutputType() == null) {
            invalidExpression(fieldPath);
        }
        return new NodeFieldExpression(node, TypeParsingContext.getExpression(node.getOutputType(), subPath));
    }

    private NodeRT<?> getNode(String nodeName) {
        return flow.getNodeByName(nodeName);
    }

    private void invalidExpression(String fieldPath) {
        throw BusinessException.invalidExpression("节点表达式'" + fieldPath + "'不正确");
    }

}
