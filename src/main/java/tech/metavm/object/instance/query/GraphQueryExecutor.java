package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionEvaluator;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.object.instance.query.PathResolver.resolvePath;

public class GraphQueryExecutor {

    public List<InstanceDTO[]> execute(ClassType type, List<Instance> instances, List<Expression> expressions, IEntityContext entityContext) {
        PathTree path = resolvePath(expressions);
        ObjectNode tree = new ObjectNode(path, type);
        loadTree(NncUtils.map(instances, i -> new NodeInstancePair(tree, i)));
        Expression[] exprArray = new Expression[expressions.size()];
        expressions.toArray(exprArray);
        List<InstanceDTO[]> results = new ArrayList<>();
        for (Instance instance : instances) {
            InstanceDTO[] value = new InstanceDTO[exprArray.length];
            results.add(value);
            for(int j = 0; j < exprArray.length; j++) {
                value[j] = ExpressionEvaluator.evaluate(exprArray[j], tree, instance, entityContext).toDTO();
            }
        }
        return results;
    }

    public void loadTree(Map<Instance, InstanceNode<?>> instance2node) {
        List<NodeInstancePair> keyValues = new ArrayList<>();
        instance2node.forEach((i, n) -> keyValues.add(new NodeInstancePair(n, i)));
        loadTree(keyValues);
    }

    private void loadTree(List<NodeInstancePair> node2instance) {
        Queue<NodeInstancePair> queue = new LinkedList<>();
        for (NodeInstancePair pair : node2instance) {
            queue.offer(pair);
        }
        while (!queue.isEmpty()) {
            NodeInstancePair pair = queue.poll();
            InstanceNode<?> node = pair.node();
            Instance instance = pair.instance();
            EntityUtils.ensureProxyInitialized(instance);
            List<NodeInstancePair> childPairs = node.getNodeInstancePairsForChildren(instance);
            for (NodeInstancePair childPair : childPairs) {
                queue.offer(childPair);
            }
        }
    }

}
