package tech.metavm.object.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.Page;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.util.Instances;
import tech.metavm.util.MultiApplicationMap;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.util.Constants.ROOT_APP_ID;
import static tech.metavm.util.ContextUtil.getAppId;

public class MemInstanceSearchServiceV2 implements InstanceSearchService {

    public static final Logger logger = LoggerFactory.getLogger(MemInstanceSearchServiceV2.class);

    private final MultiApplicationMap<Id, Source> sourceMap = new MultiApplicationMap<>();

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

    private static <T> List<T> getPage(List<T> result, int start, int end) {
        if (start >= result.size())
            return List.of();
        return result.subList(start, Math.min(end, result.size()));
    }

    private boolean match(Source source, SearchQuery query) {
        if (!query.types().contains(((PhysicalId) source.id()).getTypeKey().toTypeExpression()))
            return false;
        return query.condition() == null || Instances.isTrue(
                query.condition().evaluate(new SourceEvaluationContext(source))
        );
    }


    public boolean contains(Id id) {
        return sourceMap.containsKey(getAppId(), id)
                || sourceMap.containsKey(ROOT_APP_ID, id);
    }

    public void add(long appId, ClassInstance instance) {
        bulk(appId, List.of(instance), List.of());
    }

    public void clear() {
        sourceMap.clear();
    }

    @Override
    public void bulk(long appId, List<ClassInstance> toIndex, List<Id> toDelete) {
        for (ClassInstance instance : toIndex) {
            NncUtils.requireNonNull(instance.tryGetPhysicalId());
            sourceMap.put(
                    appId,
                    instance.tryGetId(),
                    buildSource(instance)
            );
        }
        for (var id : toDelete) {
            sourceMap.remove(appId, id);
            sourceMap.remove(appId, id);
        }
    }

    private Source buildSource(ClassInstance instance) {
        var fields = new HashMap<Id, FieldValue>();
        instance.forEachField((field, value) -> {
            if (!field.isChild())
                fields.put(field.getTag(), value.toFieldValueDTO());
        });
        return new Source(
                instance.getId(),
                fields
        );
    }

    public MemInstanceSearchServiceV2 copy() {
        var copy = new MemInstanceSearchServiceV2();
        copy.sourceMap.putAll(sourceMap);
        return copy;
    }

}
