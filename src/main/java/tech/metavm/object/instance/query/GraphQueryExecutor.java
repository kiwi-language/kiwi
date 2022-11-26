package tech.metavm.object.instance.query;

import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.instance.query.PathResolver.resolvePath;

public class GraphQueryExecutor {

    private final InstanceContext context;

    public GraphQueryExecutor(InstanceContext context) {
        this.context = context;
    }

    public List<Object[]> execute(List<Long> instanceIds, List<Expression> expressions) {
        Path path = resolvePath(expressions);
        List<ObjectTree> roots = new ArrayList<>();
        for (Long instanceId : instanceIds) {
            roots.add(new ObjectTree(path, instanceId));
        }
        loadTree(roots);
        Expression[] exprArray = new Expression[expressions.size()];
        expressions.toArray(exprArray);
        List<Object[]> results = new ArrayList<>();
        for (ObjectTree root : roots) {
            Object[] value = new Object[exprArray.length];
            results.add(value);
            for(int j = 0; j < exprArray.length; j++) {
                value[j] = ExpressionEvaluator.evaluate(exprArray[j], root);
            }
        }
        return results;
    }

    private void loadTree(List<ObjectTree> trees) {
        List<Long> ids = NncUtils.map(trees, ObjectTree::getInstanceId);
        List<IInstance> instances = context.batchGet(ids);
        Map<Long, IInstance> instanceMap = NncUtils.toMap(instances, IInstance::getId);

        for (ObjectTree tree : trees) {
            IInstance instance = instanceMap.get(tree.getInstanceId());
            if(instance != null) {
                tree.setInstance(instance);
            }
        }

        List<ObjectTree> nextLevel = NncUtils.flatMap(trees, ObjectTree::getChildObjectTrees);
        if(NncUtils.isNotEmpty(nextLevel)) {
            loadTree(nextLevel);
        }
    }

}
