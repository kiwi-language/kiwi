package tech.metavm.object.instance;

import tech.metavm.common.Page;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.search.IndexSourceBuilder;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.util.Instances;
import tech.metavm.util.MultiApplicationMap;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.util.Constants.ROOT_APP_ID;
import static tech.metavm.util.TestContext.getAppId;

public class MemInstanceSearchService implements InstanceSearchService {

    private final MultiApplicationMap<Long, Map<String, Object>> sourceMap = new MultiApplicationMap<>();
    private final MultiApplicationMap<Long, ClassInstance> instanceMap = new MultiApplicationMap<>();

    @Override
    public Page<Long> search(SearchQuery query) {
        List<Long> result = new ArrayList<>();
        doSearch(getAppId(), query, result);
        doSearch(ROOT_APP_ID, query, result);
        Collections.sort(result);
        return new Page<>(
                getPage(result, query.from(), query.end()),
                result.size()
        );
    }

    @Override
    public long count(SearchQuery query) {
        List<Long> result = new ArrayList<>();
        doSearch(getAppId(), query, result);
        doSearch(ROOT_APP_ID, query, result);
        return result.size();
    }

    private void doSearch(long appId, SearchQuery query, List<Long> result) {
        Collection<ClassInstance> instances = instanceMap.values(appId);
        for (ClassInstance instance : instances) {
            if(match(instance, query)) {
                result.add(instance.getId());
            }
        }
    }

    public boolean contains(long id) {
        return instanceMap.containsKey(getAppId(), id)
                || instanceMap.containsKey(ROOT_APP_ID, id);
    }

    private static <T> List<T> getPage(List<T> result, int start, int end) {
        if(start >= result.size()) {
            return List.of();
        }
        return result.subList(start, Math.min(end, result.size()));
    }

    private boolean match(ClassInstance instance, SearchQuery query) {
        if(!query.typeIds().contains(instance.getType().getId())) {
            return false;
        }
        return Instances.isTrue(
                query.condition().evaluate(new InstanceEvaluationContext(instance, null))
        );
    }

    public void add(long appId, ClassInstance instance) {
        bulk(appId, List.of(instance), List.of());
    }

    public void remove(long appId, long id) {
        bulk(appId, List.of(), List.of(id));
    }

    @Override
    public void bulk(long appId, List<ClassInstance> toIndex, List<Long> toDelete) {
        for (ClassInstance instance : toIndex) {
            NncUtils.requireNonNull(instance.getId());
            sourceMap.put(
                    getAppId(),
                    instance.getId(),
                    IndexSourceBuilder.buildSource(appId, instance)
            );
            instanceMap.put(getAppId(), instance.getId(), instance);
        }
        for (Long id : toDelete) {
            sourceMap.remove(getAppId(), id);
            instanceMap.remove(getAppId(), id);
        }
    }

    public void clear() {
        instanceMap.clear();
        sourceMap.clear();

    }
}
