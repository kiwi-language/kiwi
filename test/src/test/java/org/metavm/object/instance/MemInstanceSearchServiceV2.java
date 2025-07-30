package org.metavm.object.instance;

import org.metavm.common.Page;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.instance.search.SearchQuery;
import org.metavm.util.Hooks;
import org.metavm.util.SearchSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import java.util.*;

import static org.metavm.util.Constants.ROOT_APP_ID;
import static org.metavm.util.ContextUtil.getAppId;

public class MemInstanceSearchServiceV2 implements InstanceSearchService {

    public static final String MAIN_ALIAS_PREFIX = "instance-main-";
    public static final String TMP_ALIAS_PREFIX = "instance-tmp-";
    public static final String BAK_ALIAS_PREFIX = "instance-bak-";


    public static final Logger logger = LoggerFactory.getLogger(MemInstanceSearchServiceV2.class);

    private final List<MemSearchIndex> indices = new ArrayList<>();
    private final Map<String, MemSearchIndex> indexMap = new HashMap<>();
    private int nextIndexId = 1;

    public MemInstanceSearchServiceV2() {
        Hooks.SEARCH_BULK = this::bulk;
        Hooks.DROP_INDICES = appId -> TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                deleteAllIndices(appId);
            }
        });
        Hooks.CREATE_INDEX_IF_NOT_EXISTS = appId -> TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                createIndexIfNotExists(appId);
            }
        });
        createSystemIndices();
    }

    public void createSystemIndices() {
        createIndex(1, false);
        createIndex(2, false);
    }

    @Override
    public void createIndex(long appId, boolean tmp) {
        var index = new MemSearchIndex("instance-" + nextIndexId++);
        indices.add(index);
        indexMap.put(index.getName(), index);
        if (tmp)
            addAlias(index, TMP_ALIAS_PREFIX + appId);
        else
            addAlias(index, MAIN_ALIAS_PREFIX + appId);
    }

    @Override
    public void createIndexIfNotExists(long appId) {
        if (findIndex(MAIN_ALIAS_PREFIX, appId) == null)
            createIndex(appId, false);
    }

    @Override
    public void switchAlias(long appId) {
        var bakIndex = findIndex(BAK_ALIAS_PREFIX, appId);
        if (bakIndex != null)
            removeIndex(bakIndex);
        var tmpIndex = getIndex(TMP_ALIAS_PREFIX, appId);
        var mainIndex = getIndex(MAIN_ALIAS_PREFIX, appId);
        removeAlias(tmpIndex, TMP_ALIAS_PREFIX + appId);
        addAlias(tmpIndex, MAIN_ALIAS_PREFIX + appId);
        removeAlias(mainIndex, MAIN_ALIAS_PREFIX + appId);
        addAlias(mainIndex, BAK_ALIAS_PREFIX + appId);
    }

    @Override
    public void deleteAllIndices(long appId) {
        var index = getIndex(MAIN_ALIAS_PREFIX, appId);
        removeIndex(index);
        var bakIndex = findIndex(BAK_ALIAS_PREFIX, appId);
        if (bakIndex != null)
            removeIndex(bakIndex);
        var tmpIndex = findIndex(TMP_ALIAS_PREFIX, appId);
        if (tmpIndex != null)
            removeIndex(tmpIndex);
    }

    private void removeAlias(MemSearchIndex index, String alias) {
        if (indexMap.get(alias) == index)
            removeIndexName(alias);
        index.removeAlias(alias);
    }

    private void addAlias(MemSearchIndex index, String alias) {
        index.addAlias(alias);
        indexMap.put(alias, index);
    }

    @Override
    public void revert(long appId) {
        var bakIndex = indexMap.get(BAK_ALIAS_PREFIX + appId);
        if (bakIndex == null)
            throw new IllegalStateException("No backup index found for appId: " + appId);
        bakIndex.removeAlias(BAK_ALIAS_PREFIX + appId);
        if (bakIndex.hasAlias(MAIN_ALIAS_PREFIX + appId))
            return;
        bakIndex.addAlias(MAIN_ALIAS_PREFIX + appId);
        var mainIndex = getIndex(MAIN_ALIAS_PREFIX, appId);
        if (mainIndex != null)
            removeIndex(mainIndex);
    }

    private void removeIndex(MemSearchIndex index) {
        indices.remove(index);
        index.forEachName(this::removeIndexName);
    }

    @Override
    public Page<Id> search(SearchQuery query) {
        if(query.size() > 999)
            throw new IllegalArgumentException("Page size cannot exceed 999");
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

    private void doSearch(long appId, SearchQuery query, List<Id> result) {
        var index = getIndex(MAIN_ALIAS_PREFIX, appId);
        result.addAll(index.search(query));
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
        return getIndex(MAIN_ALIAS_PREFIX, getAppId()).contains(id + "")
                || getIndex(MAIN_ALIAS_PREFIX, ROOT_APP_ID).contains(id + "");
    }

    private MemSearchIndex getIndex(String prefix, long appId) {
        return Objects.requireNonNull(findIndex(prefix, appId), () -> "Index " + prefix + appId + " not found");
    }

    private @Nullable MemSearchIndex findIndex(String prefix, long appId) {
        return indexMap.get(prefix + appId);
    }

    public void add(long appId, ClassInstance instance) {
        bulk(new SearchSyncRequest(appId, false, List.of(instance), List.of(), false));
    }

    public void clear() {
        indexMap.clear();
    }

    private void removeIndexName(String name) {
        indexMap.remove(name);
    }

    @Override
    public void bulk(SearchSyncRequest request) {
        var index = request.migrating() ?
                getIndex(TMP_ALIAS_PREFIX, request.appId()) : getIndex(MAIN_ALIAS_PREFIX, request.appId());
        for (ClassInstance instance : request.changedInstances()) {
            Objects.requireNonNull(instance.tryGetTreeId());
            index.index(
                    instance.getTreeId() + "",
                    buildSource(instance)
            );
        }
        for (var id : request.removedInstanceIds()) {
            index.remove(id.getTreeId() + "");
            index.remove(id.getTreeId() + "");
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
        for (var index : indices) {
            var indexCopy = index.copy();
            copy.indices.add(indexCopy);
            indexMap.put(indexCopy.getName(), indexCopy);
            indexCopy.getAliases().forEach(alias -> copy.indexMap.put(alias, indexCopy));
        }
        return copy;
    }

}
