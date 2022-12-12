package tech.metavm.object.instance.query;

import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

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
            roots.add(new ObjectTree(path, (ClassInstance) context.get(instanceId)));
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

    private void loadTree(List<? extends NTree> trees) {
        NncUtils.forEach(trees, NTree::load);

        List<NTree> nextLevel = NncUtils.flatMap(trees, NTree::getChildren);
        if(NncUtils.isNotEmpty(nextLevel)) {
            loadTree(nextLevel);
        }
    }

}
