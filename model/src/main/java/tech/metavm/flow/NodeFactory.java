package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.ExpressionTypeMap;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;

import static tech.metavm.util.ReflectionUtils.tryGetStaticMethod;

public class NodeFactory {

    public static NodeRT save(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("NodeFactory.save")) {
            NodeKind nodeType = NodeKind.getByCodeRequired(nodeDTO.kind());
            Class<? extends NodeRT> klass = nodeType.getNodeClass();
            Method createMethod = tryGetStaticMethod(
                    klass, "save", NodeDTO.class, NodeRT.class, ScopeRT.class, IEntityContext.class);
            NodeRT prev = nodeDTO.prevId() != null ? context.getNode(nodeDTO.prevId()) : scope.getLastNode();
            NodeRT node = context.getNode(Id.parse(nodeDTO.id()));
            boolean isCreate = node == null;
            if(node != null) {
                node.setName(nodeDTO.name());
                node.setCode(nodeDTO.code());
            }
            node = (NodeRT) ReflectionUtils.invoke(null, createMethod, nodeDTO, prev, scope, context);
            var prevExprTypes = prev != null ? prev.getExpressionTypes() : ExpressionTypeMap.EMPTY;
            node.mergeExpressionTypes(prevExprTypes);
            if(isCreate)
                context.bind(node);
            return node;
        }
    }

}
