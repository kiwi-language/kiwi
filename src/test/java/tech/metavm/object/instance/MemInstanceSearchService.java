package tech.metavm.object.instance;

import tech.metavm.dto.Page;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionEvaluator;
import tech.metavm.object.instance.query.InstanceEvaluationContext;
import tech.metavm.object.instance.search.IndexSourceBuilder;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.MultiTenantMap;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;
import static tech.metavm.util.TestContext.getTenantId;

public class MemInstanceSearchService implements InstanceSearchService {

    private final MultiTenantMap<Long, Map<String, Object>> sourceMap = new MultiTenantMap<>();
    private final MultiTenantMap<Long, ClassInstance> instanceMap = new MultiTenantMap<>();

    @Override
    public Page<Long> search(SearchQuery query) {
        List<Long> result = new ArrayList<>();
        doSearch(getTenantId(), query.condition(), result);
        doSearch(ROOT_TENANT_ID, query.condition(), result);
        Collections.sort(result);
        return new Page<>(
                getPage(result, query.from(), query.end()),
                result.size()
        );
    }

    private void doSearch(long tenantId, Expression condition, List<Long> result) {
        for (ClassInstance instance : instanceMap.values(tenantId)) {
            if(match(instance, condition)) {
                result.add(instance.getId());
            }
        }
    }

    public boolean contains(long id) {
        return instanceMap.containsKey(getTenantId(), id)
                || instanceMap.containsKey(ROOT_TENANT_ID, id);
    }

    private static <T> List<T> getPage(List<T> result, int start, int end) {
        if(start >= result.size()) {
            return List.of();
        }
        return result.subList(start, Math.min(end, result.size()));
    }

    private boolean match(ClassInstance instance, Expression condition) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(
                condition, new InstanceEvaluationContext(instance), true
        );
        return InstanceUtils.isTrue(evaluator.evaluate());
    }

    public void add(long tenantId, ClassInstance instance) {
        bulk(tenantId, List.of(instance), List.of());
    }

    public void remove(long tenantId, long id) {
        bulk(tenantId, List.of(), List.of(id));
    }

    @Override
    public void bulk(long tenantId, List<ClassInstance> toIndex, List<Long> toDelete) {
        for (ClassInstance instance : toIndex) {
            NncUtils.requireNonNull(instance.getId());
            sourceMap.put(
                    getTenantId(),
                    instance.getId(),
                    IndexSourceBuilder.buildSource(tenantId, instance)
            );
            instanceMap.put(getTenantId(), instance.getId(), instance);
        }
        for (Long id : toDelete) {
            sourceMap.remove(getTenantId(), id);
            instanceMap.remove(getTenantId(), id);
        }
    }
}
