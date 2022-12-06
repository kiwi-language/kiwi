package tech.metavm.object.instance;

import tech.metavm.dto.Page;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionEvaluator;
import tech.metavm.object.instance.search.IndexSourceBuilder;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.util.MultiTenantMap;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TestContext;

import java.util.List;
import java.util.Map;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;

public class MemInstanceSearchService implements InstanceSearchService {

    private final MultiTenantMap<Long, Map<String, Object>> instanceMap = new MultiTenantMap<>();

    @Override
    public Page<Long> search(SearchQuery query) {
//        List<Long> result = new ArrayList<>();
//        for (Map<String, Object> sources : instanceMap.values()) {
//            if(match(instance, query.condition())) {
//                result.add(instance.getId());
//            }
//        }
//        return new Page<>(
//                getPage(result, query.from(), query.end()),
//                result.size()
//        );

        return Page.empty();
    }

    public boolean contains(long id) {
        return instanceMap.containsKey(TestContext.getTenantId(), id)
                || instanceMap.containsKey(ROOT_TENANT_ID, id);
    }

    private static <T> List<T> getPage(List<T> result, int start, int end) {
        if(start >= result.size()) {
            return List.of();
        }
        return result.subList(start, Math.min(end, result.size()));
    }

    private boolean match(Instance instance, Expression condition) {
        return Boolean.TRUE.equals(ExpressionEvaluator.evaluate(condition, instance));
    }

    @Override
    public void bulk(long tenantId, List<Instance> toIndex, List<Long> toDelete) {
        for (Instance instance : toIndex) {
            NncUtils.requireNonNull(instance.getId());
            instanceMap.put(
                    TestContext.getTenantId(),
                    instance.getId(),
                    IndexSourceBuilder.buildSource(tenantId, instance)
            );
        }
        for (Long id : toDelete) {
            instanceMap.remove(TestContext.getTenantId(), id);
        }
    }
}
