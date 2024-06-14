package org.metavm.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.util.DebugEnv;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.metavm.util.ReflectionUtils.tryGetStaticMethod;

public class NodeFactory {

    public static final Logger logger = LoggerFactory.getLogger(NodeFactory.class);

    public static NodeRT save(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("NodeFactory.save")) {
            NodeKind nodeType = NodeKind.getByCodeRequired(nodeDTO.kind());
            Class<? extends NodeRT> klass = nodeType.getNodeClass();
            Method createMethod = tryGetStaticMethod(
                    klass, "save", NodeDTO.class, NodeRT.class, ScopeRT.class, IEntityContext.class);
            NodeRT prev = nodeDTO.prevId() != null ? context.getNode(nodeDTO.prevId()) : scope.getLastNode();
            NodeRT node = context.getNode(nodeDTO.id());
            boolean isCreate = node == null;
            if (node != null) {
                node.setName(nodeDTO.name());
                node.setCode(nodeDTO.code());
            }
            try {
                node = (NodeRT) ReflectionUtils.invoke(null, createMethod, nodeDTO, prev, scope, context);
                var prevExprTypes = prev != null ? prev.getExpressionTypes() : ExpressionTypeMap.EMPTY;
                node.mergeExpressionTypes(prevExprTypes);
                if (isCreate)
                    context.bind(node);
                return node;
            } catch (RuntimeException e) {
                if (DebugEnv.debugging)
                    DebugEnv.logger.info("fail to save node {}", NncUtils.toJSONString(nodeDTO));
                throw e;
            }
        }
    }

}
