package org.metavm.object.instance;

import org.metavm.common.Page;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.instance.search.SearchQuery;
import org.metavm.util.Hooks;
import org.metavm.util.MultiApplicationMap;
import org.metavm.util.SearchSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.metavm.util.Constants.ROOT_APP_ID;
import static org.metavm.util.ContextUtil.getAppId;

public class MemInstanceSearchServiceV2 implements InstanceSearchService {

    public static final Logger logger = LoggerFactory.getLogger(MemInstanceSearchServiceV2.class);

    private final MultiApplicationMap<Long, Source> sourceMap = new MultiApplicationMap<>();

    public MemInstanceSearchServiceV2() {
        Hooks.SEARCH_BULK = this::bulk;
    }

    @Override
    public Page<Id> search(SearchQuery query) {
        List<Id> result = new ArrayList<>();
        doSearch(query.appId(), query, result);
        if (query.includeBuiltin())
            doSearch(ROOT_APP_ID, query, result);
        Collections.sort(result);
        return new Page<>(
                getPage(result, query.from(), query.end()),
                result.size()
        );
    }

    @Override
    public long count(SearchQuery query) {
        List<Id> result = new ArrayList<>();
        doSearch(query.appId(), query, result);
        if (query.includeBuiltin())
            doSearch(ROOT_APP_ID, query, result);
        return result.size();
    }

    private void doSearch(Long appId, SearchQuery query, List<Id> result) {
        Collection<Source> sources = sourceMap.values(appId);
        for (var source : sources) {
            if (match(source, query))
                result.add(source.id());
        }
    }

    private static <T extends Comparable<T>> List<T> getPage(List<T> result, int start, int end) {
        if (start >= result.size() || end <= start)
            return List.of();
        return result.stream().sorted(Comparator.reverseOrder()).skip(start).limit(end - start + 1).toList();
    }

    private boolean match(Source source, SearchQuery query) {
        if (!query.types().contains(source.typeKey().toTypeExpression()))
            return false;
        return query.condition() == null || query.condition().evaluate(source.fields());
    }


    public boolean contains(long id) {
        return sourceMap.containsKey(getAppId(), id)
                || sourceMap.containsKey(ROOT_APP_ID, id);
    }

    public void add(long appId, ClassInstance instance) {
        bulk(new SearchSyncRequest(appId, List.of(instance), List.of(), false));
    }

    public void clear() {
        sourceMap.clear();
    }

    @Override
    public void bulk(SearchSyncRequest request) {
        for (ClassInstance instance : request.changedInstances()) {
            Objects.requireNonNull(instance.tryGetTreeId());
            sourceMap.put(
                    request.appId(),
                    instance.getTreeId(),
                    buildSource(instance)
            );
        }
        for (var id : request.removedInstanceIds()) {
            sourceMap.remove(request.appId(), id.getTreeId());
            sourceMap.remove(request.appId(), id.getTreeId());
        }
    }

    private Source buildSource(ClassInstance instance) {
        var fields = instance.buildSource();
        return new Source(
                instance.getId(),
                instance.getInstanceType().toTypeKey(),
                fields
        );
    }

    public MemInstanceSearchServiceV2 copy() {
        var copy = new MemInstanceSearchServiceV2();
        copy.sourceMap.putAll(sourceMap);
        return copy;
    }

}
