package org.metavm.object.instance.search;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xcontent.XContentType;
import org.metavm.common.ErrorCode;
import org.metavm.common.Page;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.BusinessException;
import org.metavm.util.Hooks;
import org.metavm.util.SearchSyncRequest;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class InstanceSearchServiceImpl implements InstanceSearchService {

    // IMPORTANT: Your application should use the ALIAS for all operations.
    // The actual index name should be treated as an implementation detail.
    public static final String INDEX_PREFIX = "instance-";
    public static final String MAIN_ALIAS_PREFIX = "instance-main-";
    public static final String TMP_ALIAS_PREFIX = "instance-tmp-";
    public static final String BAK_ALIAS_PREFIX = "instance-bak-";

    private final RestHighLevelClient restHighLevelClient;

    public InstanceSearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
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
    }

    @Override
    public void createIndex(long appId, boolean tmp) {
        try {
            var indexName = INDEX_PREFIX + appId + "-v" + getVersion(appId);

            // First, create the physical index
            createIndex(indexName);

            // Then, point the public-facing alias to this new index
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            var prefix = tmp ? TMP_ALIAS_PREFIX : MAIN_ALIAS_PREFIX;
            IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(indexName)
                            .alias(prefix + appId);
            request.addAliasAction(aliasAction);
            restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);
            log.info("Successfully pointed alias '{}' to new index '{}'.", indexName, indexName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create index with alias", e);
        }
    }

    @Override
    public void createIndexIfNotExists(long appId) {
        String mainAlias = MAIN_ALIAS_PREFIX + appId;
        String existingIndex = getIndexForAlias(mainAlias);
        if (existingIndex == null) {
            log.info("Main alias '{}' does not exist for app [{}]. Creating new index and alias.", mainAlias, appId);
            createIndex(appId, false);
        } else {
            log.info("Main alias '{}' already exists for app [{}] and points to index '{}'. No action taken.", mainAlias, appId, existingIndex);
        }
    }

    private long getVersion(long appId) throws IOException {
        String indexSearchPattern = String.format("%s%d-v*", INDEX_PREFIX, appId);
        GetIndexRequest request = new GetIndexRequest(indexSearchPattern);
        String[] indices;

        try {
            GetIndexResponse response = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
            indices = response.getIndices();
        } catch (ElasticsearchStatusException e) {
            // This exception is thrown if the wildcard expression matches no indices.
            if (e.status() == RestStatus.NOT_FOUND) {
                log.info("No existing indices found for pattern '{}'. Starting with version 1.", indexSearchPattern);
                return 1L;
            }
            // Re-throw other unexpected exceptions.
            throw e;
        }

        if (indices.length == 0) {
            return 1L; // Should be covered by the catch block, but here for safety.
        }

        long maxVersion = Arrays.stream(indices)
                .map(name -> {
                    try {
                        // Find the last occurrence of '-v' and parse the number that follows.
                        int versionMarker = name.lastIndexOf("-v");
                        if (versionMarker != -1) {
                            return Long.parseLong(name.substring(versionMarker + 2));
                        }
                    } catch (NumberFormatException ex) {
                        log.warn("Could not parse version from malformed index name: '{}'", name);
                    }
                    return -1L; // Return a sentinel value for names that can't be parsed.
                })
                .filter(v -> v != -1L) // Filter out any malformed names.
                .max(Long::compare)
                .orElse(0L); // If all names were malformed, start from 0.

        return maxVersion + 1;
    }

    @Override
    public void switchAlias(long appId) {
        String mainAlias = MAIN_ALIAS_PREFIX + appId;
        String tmpAlias = TMP_ALIAS_PREFIX + appId;
        String bakAlias = BAK_ALIAS_PREFIX + appId;

        try {
            // --- Step 1: Remove the index with the alias instance-bak-{appId} if it exists ---
            log.info("App [{}]: Starting alias switch process. Step 1: Cleaning up old backup.", appId);
            String oldBakIndexName = getIndexForAlias(bakAlias);
            if (oldBakIndexName != null) {
                log.info("App [{}]: Found old backup index '{}' (alias: {}). Deleting it now.", appId, oldBakIndexName, bakAlias);
                deleteIndex(oldBakIndexName);
            } else {
                log.info("App [{}]: No old backup index found. Proceeding.", appId);
            }

            // --- Steps 2 & 3: Find indices and perform the atomic alias switch ---
            log.info("App [{}]: Step 2 & 3: Preparing to swap main and temporary aliases.", appId);

            // Find the physical index names we are working with.
            String mainIndexName = getIndexForAlias(mainAlias);
            String tmpIndexName = getIndexForAlias(tmpAlias);

            // Sanity checks before proceeding with the swap.
            if (tmpIndexName == null) {
                log.error("App [{}]: ABORTING SWITCH. The temporary alias '{}' does not point to an index. A new index must be prepared first.", appId, tmpAlias);
                return;
            }
            if (mainIndexName == null) {
                log.error("App [{}]: ABORTING SWITCH. The main service alias '{}' does not point to an index. Cannot perform a switch.", appId, mainAlias);
                return;
            }
            if (mainIndexName.equals(tmpIndexName)) {
                log.warn("App [{}]: ABORTING SWITCH. Main alias '{}' and temporary alias '{}' already point to the same index ('{}'). No action needed.", appId, mainAlias, tmpAlias, mainIndexName);
                return;
            }

            // Build the atomic request that performs both renames at once.
            IndicesAliasesRequest request = new IndicesAliasesRequest();

            // Step 2: Rename instance-service-{appId} to instance-bak-{appId}
            request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE).index(mainIndexName).alias(mainAlias));
            request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD).index(mainIndexName).alias(bakAlias));

            // Step 3: Rename instance-tmp-{appId} to instance-service-{appId}
            request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE).index(tmpIndexName).alias(tmpAlias));
            request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD).index(tmpIndexName).alias(mainAlias));

            // Execute the atomic switch.
            AcknowledgedResponse response = restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);

            if (response.isAcknowledged()) {
                log.info("App [{}]: SWITCH COMPLETE. Main service is now on index '{}'. Old index '{}' is now the backup.", appId, tmpIndexName, mainIndexName);
            } else {
                log.error("App [{}]: FAILED TO SWITCH ALIASES. The atomic operation was not acknowledged by the cluster.", appId);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error during alias switch for app: " + appId, e);
        }
    }

    @SneakyThrows
    private void deleteIndex(String name) {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest(name);
        restHighLevelClient.indices().delete(deleteRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void deleteAllIndices(long appId) {
        var mainIndex = getIndexForAlias(MAIN_ALIAS_PREFIX + appId);
        if (mainIndex != null) {
            deleteIndex(mainIndex);
            log.info("App [{}]: Deleted main index '{}'.", appId, mainIndex);
        } else {
            log.warn("App [{}]: No main index found to delete.", appId);
        }
        var tmpIndex = getIndexForAlias(TMP_ALIAS_PREFIX + appId);
        if (tmpIndex != null) {
            deleteIndex(tmpIndex);
            log.info("App [{}]: Deleted temporary index '{}'.", appId, tmpIndex);
        }
        var bakIndex = getIndexForAlias(BAK_ALIAS_PREFIX + appId);
        if (bakIndex != null) {
            deleteIndex(bakIndex);
            log.info("App [{}]: Deleted backup index '{}'.", appId, bakIndex);
        }
    }

    @Override
    public void revert(long appId) {
        var bakAlias = BAK_ALIAS_PREFIX + appId;
        var bakIndex = getIndexForAlias(bakAlias);
        if (bakIndex == null)
            throw new BusinessException(ErrorCode.NO_BACKUP, appId);
        var mainAlias = MAIN_ALIAS_PREFIX + appId;
        try {
            var mainIndex = getIndexForAlias(mainAlias);

            IndicesAliasesRequest request = new IndicesAliasesRequest();
            if (mainIndex != null) {
                request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                        .index(mainIndex)
                        .alias(mainAlias));
            }
            request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(bakIndex)
                    .alias(bakAlias));
            request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                    .index(bakIndex)
                    .alias(mainAlias));
            restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);
            if (mainIndex != null && !mainIndex.equals(bakIndex)) {
                DeleteIndexRequest deleteRequest = new DeleteIndexRequest(mainIndex);
                restHighLevelClient.indices().delete(deleteRequest, RequestOptions.DEFAULT);
                log.info("App [{}]: Deleted original main index '{}'.", appId, mainIndex);
            } else {
                log.info("App [{}]: No original main index to delete or it is the same as backup '{}'.", appId, bakIndex);
            }
            log.info("App [{}]: Rolled back. Main alias '{}' now points to backup index '{}'.", appId, mainAlias, bakIndex);
        } catch (IOException e) {
            throw new RuntimeException("Error during rollback for app: " + appId, e);
        }

    }

    @SneakyThrows
    private String getIndexForAlias(String aliasName) {
        GetAliasesRequest request = new GetAliasesRequest(aliasName);
        try {
            var response = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
            Set<String> indexNames = response.getAliases().keySet();
            if (indexNames.size() > 1) {
                throw new RuntimeException("Critical error: Alias '" + aliasName + "' points to multiple indices: " + indexNames);
            }
            return indexNames.stream().findFirst().orElse(null);
        } catch (ElasticsearchStatusException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                // This is expected if the alias doesn't exist yet.
                return null;
            }
            throw e;
        }
    }

    // Previous methods remain for direct index manipulation when needed

    @Override
    public Page<Id> search(SearchQuery query) {
        var alias = MAIN_ALIAS_PREFIX + query.appId();
        SearchRequest searchRequest = new SearchRequest(alias);
        searchRequest.routing(query.appId() + (query.includeBuiltin() ? ",-1" : ""));
        searchRequest.source(SearchBuilder.build(query));
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long total = response.getHits().getTotalHits().value;
            List<Id> ids = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                var id = PhysicalId.of(Long.parseLong(hit.getId()), 0L);
//                var typeId = ((Number) hit.getSourceAsMap().get("typeId")).longValue();
                ids.add(id);
            }
            return new Page<>(ids, total);
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    @Override
    public long count(SearchQuery query) {
        CountRequest countRequest = new CountRequest(INDEX_PREFIX);
        countRequest.routing(query.appId() + (query.includeBuiltin() ? ",-1" : ""));
        countRequest.source(SearchBuilder.build(query));
        try {
            var response = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
            return response.getCount();
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    @Override
    public void bulk(SearchSyncRequest request) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(request.waitUtilRefresh() ? WriteRequest.RefreshPolicy.WAIT_UNTIL : WriteRequest.RefreshPolicy.NONE);
        var appId = request.appId();
        List<IndexRequest> indexRequests = Utils.map(request.changedInstances(), instance -> buildIndexRequest(appId, request.migrating(), instance));
        List<DeleteRequest> deleteRequests = Utils.map(request.removedInstanceIds(), id -> buildDeleteRequest(appId, request.migrating(), id));
        indexRequests.forEach(bulkRequest::add);
        deleteRequests.forEach(bulkRequest::add);
        try {
            var resp = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (resp.hasFailures()) {
                log.error("Failed to synchronized ES documents: {}", resp.buildFailureMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    private IndexRequest buildIndexRequest(long appId, boolean migrating, ClassInstance instance) {
        var alias = migrating ? TMP_ALIAS_PREFIX + appId : MAIN_ALIAS_PREFIX + appId;
        IndexRequest indexRequest = new IndexRequest(alias);
        indexRequest.id(instance.getTreeId() + "");
        indexRequest.routing(appId + "");
        indexRequest.source(IndexSourceBuilder.buildSource(appId, instance));
        return indexRequest;
    }

    private DeleteRequest buildDeleteRequest(long appId, boolean migrating, Id id) {
        var alias = migrating ? TMP_ALIAS_PREFIX + appId : MAIN_ALIAS_PREFIX + appId;
        DeleteRequest deleteRequest = new DeleteRequest(alias);
        deleteRequest.id(Long.toString(id.getTreeId()));
        deleteRequest.routing(appId + "");
        return deleteRequest;
    }

    private void createIndex(String indexName) throws IOException {
        boolean exists = restHighLevelClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.source(getIndexSource(), XContentType.JSON);
            restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info("Successfully created index '{}' with custom mapping.", indexName);
        } else {
            log.info("Index '{}' already exists.", indexName);
        }
    }

    private String getIndexSource() {
        return EsInit.source;
    }
}