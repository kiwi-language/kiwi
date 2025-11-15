package org.metavm.object.instance.search;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.jsonk.Jsonk;
import org.metavm.common.ErrorCode;
import org.metavm.common.Page;
import org.metavm.context.Component;
import org.metavm.jdbc.TransactionCallback;
import org.metavm.jdbc.TransactionStatus;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.BusinessException;
import org.metavm.util.Hooks;
import org.metavm.util.SearchSyncRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Component
public class InstanceSearchServiceImpl implements InstanceSearchService {

    public static final String INDEX_PREFIX = "instance-";
    public static final String MAIN_ALIAS_PREFIX = "instance-main-";
    public static final String TMP_ALIAS_PREFIX = "instance-tmp-";
    public static final String BAK_ALIAS_PREFIX = "instance-bak-";

    private final RestClient restClient;

    public InstanceSearchServiceImpl(RestClient restClient) {
        this.restClient = restClient;
        Hooks.SEARCH_BULK = this::bulk;
        Hooks.DROP_INDICES = appId -> TransactionStatus.registerCallback(new TransactionCallback() {
            @Override
            public void afterCommit() {
                deleteAllIndices(appId);
            }
        });
        Hooks.CREATE_INDEX_IF_NOT_EXISTS = appId -> TransactionStatus.registerCallback(new TransactionCallback() {
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
            createPhysicalIndex(indexName);

            // Then, point the public-facing alias to this new index
            var prefix = tmp ? TMP_ALIAS_PREFIX : MAIN_ALIAS_PREFIX;
            String aliasName = prefix + appId;

            Map<String, Object> addAction = Map.of(
                    "add", Map.of("index", indexName, "alias", aliasName)
            );
            Map<String, Object> body = Map.of("actions", List.of(addAction));

            Request request = new Request("POST", "/_aliases");
            request.setJsonEntity(Jsonk.toJson(body));
            restClient.performRequest(request);

            log.info("Successfully pointed alias '{}' to new index '{}'.", aliasName, indexName);
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

        Request request = new Request("GET", "/" + indexSearchPattern);

        try {
            Response response = restClient.performRequest(request);
            // Result is a Map<String, Object> where keys are index names
            Map<String, Object> indicesMap = Jsonk.fromJson(new InputStreamReader(response.getEntity().getContent()), Map.class);

            if (indicesMap == null || indicesMap.isEmpty()) return 1L;

            long maxVersion = indicesMap.keySet().stream()
                    .map(name -> {
                        try {
                            int versionMarker = name.lastIndexOf("-v");
                            if (versionMarker != -1) {
                                return Long.parseLong(name.substring(versionMarker + 2));
                            }
                        } catch (NumberFormatException ex) {
                            log.warn("Could not parse version from malformed index name: '{}'", name);
                        }
                        return -1L;
                    })
                    .filter(v -> v != -1L)
                    .max(Long::compare)
                    .orElse(0L);

            return maxVersion + 1;

        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                log.info("No existing indices found for pattern '{}'. Starting with version 1.", indexSearchPattern);
                return 1L;
            }
            throw e;
        }
    }

    @Override
    public void deleteTmpIndex(long appId) {
        var tmpAlias = TMP_ALIAS_PREFIX + appId;
        var oldTmpIndexNames = getIndicesForAlias(tmpAlias);
        if (!oldTmpIndexNames.isEmpty()) {
            log.info("App [{}]: Found old tmp index {} (alias: {}). Deleting it now", appId, oldTmpIndexNames, tmpAlias);
            deleteIndex(oldTmpIndexNames.toArray(String[]::new));
        }
    }

    @Override
    public void switchAlias(long appId, boolean backup) {
        String mainAlias = MAIN_ALIAS_PREFIX + appId;
        String tmpAlias = TMP_ALIAS_PREFIX + appId;
        String bakAlias = BAK_ALIAS_PREFIX + appId;

        try {
            log.info("App [{}]: Starting alias switch process.", appId);
            if (backup) {
                String oldBakIndexName = getIndexForAlias(bakAlias);
                if (oldBakIndexName != null) {
                    deleteIndex(oldBakIndexName);
                }
            }

            String mainIndexName = getIndexForAlias(mainAlias);
            String tmpIndexName = getIndexForAlias(tmpAlias);

            if (tmpIndexName == null || mainIndexName == null) {
                log.error("App [{}]: ABORTING SWITCH. Missing indices. Main: {}, Tmp: {}", appId, mainIndexName, tmpIndexName);
                return;
            }
            if (mainIndexName.equals(tmpIndexName)) {
                return; // Already pointing to same
            }

            List<Map<String, Object>> actions = new ArrayList<>();

            actions.add(Map.of("remove", Map.of("index", mainIndexName, "alias", mainAlias)));

            if (backup) {
                actions.add(Map.of("add", Map.of("index", mainIndexName, "alias", bakAlias)));
            }

            actions.add(Map.of("remove", Map.of("index", tmpIndexName, "alias", tmpAlias)));
            actions.add(Map.of("add", Map.of("index", tmpIndexName, "alias", mainAlias)));

            Map<String, Object> body = Map.of("actions", actions);

            Request request = new Request("POST", "/_aliases");
            request.setJsonEntity(Jsonk.toJson(body));

            Response response = restClient.performRequest(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                log.info("App [{}]: SWITCH COMPLETE.", appId);
            }

            if (!backup) {
                deleteIndex(mainIndexName);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error during alias switch for app: " + appId, e);
        }
    }

    @SneakyThrows
    private void deleteIndex(String... names) {
        if (names == null || names.length == 0) return;
        String joinedNames = String.join(",", names);
        Request request = new Request("DELETE", "/" + joinedNames);
        try {
            restClient.performRequest(request);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() != 404) {
                throw e;
            }
        }
    }

    @Override
    public void deleteAllIndices(long appId) {
        deleteIndexByAlias(MAIN_ALIAS_PREFIX + appId);
        deleteIndexByAlias(TMP_ALIAS_PREFIX + appId);
        deleteIndexByAlias(BAK_ALIAS_PREFIX + appId);
    }

    private void deleteIndexByAlias(String alias) {
        String idx = getIndexForAlias(alias);
        if (idx != null) {
            deleteIndex(idx);
            log.info("Deleted index for alias '{}': '{}'", alias, idx);
        }
    }

    @Override
    public void revert(long appId) {
        var bakAlias = BAK_ALIAS_PREFIX + appId;
        var bakIndex = getIndexForAlias(bakAlias);
        if (bakIndex == null) throw new BusinessException(ErrorCode.NO_BACKUP, appId);

        var mainAlias = MAIN_ALIAS_PREFIX + appId;
        var mainIndex = getIndexForAlias(mainAlias);

        try {
            List<Map<String, Object>> actions = new ArrayList<>();

            if (mainIndex != null) {
                actions.add(Map.of("remove", Map.of("index", mainIndex, "alias", mainAlias)));
            }
            actions.add(Map.of("remove", Map.of("index", bakIndex, "alias", bakAlias)));
            actions.add(Map.of("add", Map.of("index", bakIndex, "alias", mainAlias)));

            Map<String, Object> body = Map.of("actions", actions);

            Request request = new Request("POST", "/_aliases");
            request.setJsonEntity(Jsonk.toJson(body));
            restClient.performRequest(request);

            if (mainIndex != null && !mainIndex.equals(bakIndex)) {
                deleteIndex(mainIndex);
            }
            log.info("App [{}]: Rolled back.", appId);
        } catch (IOException e) {
            throw new RuntimeException("Error during rollback for app: " + appId, e);
        }
    }

    private String getIndexForAlias(String aliasName) {
        var indexNames = getIndicesForAlias(aliasName);
        if (indexNames.isEmpty()) return null;
        if (indexNames.size() > 1)
            throw new RuntimeException("Critical error: Alias '" + aliasName + "' points to multiple indices: " + indexNames);
        return indexNames.iterator().next();
    }

    @SneakyThrows
    private Collection<String> getIndicesForAlias(String aliasName) {
        Request request = new Request("GET", "/_alias/" + aliasName);
        try {
            Response response = restClient.performRequest(request);
            Map<String, Object> root = Jsonk.fromJson(new InputStreamReader(response.getEntity().getContent()), Map.class);
            return root.keySet();
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Id> search(SearchQuery query) {
        var alias = MAIN_ALIAS_PREFIX + query.appId();
        String routing = query.appId() + (query.includeBuiltin() ? ",-1" : "");
        String endpoint = "/" + alias + "/_search?routing=" + routing;

        Request request = new Request("POST", endpoint);

        // SearchBuilder now returns Map<String, Object>, Jsonk converts it to String
        request.setJsonEntity(Jsonk.toJson(SearchBuilder.build(query)));

        try {
            Response response = restClient.performRequest(request);
            Map<String, Object> root = Jsonk.fromJson(new InputStreamReader(response.getEntity().getContent()), Map.class);

            Map<String, Object> hitsWrapper = (Map<String, Object>) root.get("hits");
            long total = ((Number) ((Map<String, Object>) hitsWrapper.get("total")).get("value")).longValue();

            List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hitsWrapper.get("hits");
            List<Id> ids = new ArrayList<>();

            for (Map<String, Object> hit : hitsList) {
                long idVal = Long.parseLong((String) hit.get("_id"));
                ids.add(PhysicalId.of(idVal, 0L));
            }
            return new Page<>(ids, total);
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    @Override
    public long count(SearchQuery query) {
        String routing = query.appId() + (query.includeBuiltin() ? ",-1" : "");
        Request request = new Request("POST", "/" + INDEX_PREFIX + "/_count?routing=" + routing);

        // IMPORTANT: SearchBuilder.build returns "from", "size" and "sort".
        // The _count API rejects these parameters. We must extract only the "query" part.
        Map<String, Object> fullSearchSource = SearchBuilder.build(query);
        Map<String, Object> countSource = new HashMap<>();
        if (fullSearchSource.containsKey("query")) {
            countSource.put("query", fullSearchSource.get("query"));
        }

        request.setJsonEntity(Jsonk.toJson(countSource));

        try {
            Response response = restClient.performRequest(request);
            Map<String, Object> root = Jsonk.fromJson(new InputStreamReader(response.getEntity().getContent()), Map.class);
            return ((Number) root.get("count")).longValue();
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    @Override
    public void bulk(SearchSyncRequest request) {
        StringBuilder ndjson = new StringBuilder();
        var appId = request.appId();
        String refresh = request.waitUtilRefresh() ? "wait_for" : "false";

        // Index requests
        for (var instance : request.changedInstances()) {
            String alias = request.migrating() ? TMP_ALIAS_PREFIX + appId : MAIN_ALIAS_PREFIX + appId;

            Map<String, Object> meta = Map.of("index", Map.of(
                    "_index", alias,
                    "_id", String.valueOf(instance.getTreeId()),
                    "routing", String.valueOf(appId)
            ));

            ndjson.append(Jsonk.toJson(meta)).append("\n");
            ndjson.append(Jsonk.toJson(IndexSourceBuilder.buildSource(appId, instance))).append("\n");
        }

        // Delete requests
        for (var id : request.removedInstanceIds()) {
            String alias = request.migrating() ? TMP_ALIAS_PREFIX + appId : MAIN_ALIAS_PREFIX + appId;

            Map<String, Object> meta = Map.of("delete", Map.of(
                    "_index", alias,
                    "_id", String.valueOf(id.getTreeId()),
                    "routing", String.valueOf(appId)
            ));

            ndjson.append(Jsonk.toJson(meta)).append("\n");
        }

        if (ndjson.length() == 0) return;

        Request bulkRequest = new Request("POST", "/_bulk?refresh=" + refresh);
        bulkRequest.setJsonEntity(ndjson.toString());

        try {
            Response response = restClient.performRequest(bulkRequest);
            Map<String, Object> root = Jsonk.fromJson(new InputStreamReader(response.getEntity().getContent()), Map.class);

            if (Boolean.TRUE.equals(root.get("errors"))) {
                log.error("Failed to synchronize ES documents. Bulk response has errors: {}", Jsonk.toJson(root));
            }
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    private void createPhysicalIndex(String indexName) throws IOException {
        try {
            restClient.performRequest(new Request("HEAD", "/" + indexName));
            log.info("Index '{}' already exists.", indexName);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                Request createRequest = new Request("PUT", "/" + indexName);
                createRequest.setJsonEntity(EsInit.source); // Assuming EsInit.source is a valid JSON String
                restClient.performRequest(createRequest);
                log.info("Successfully created index '{}' with custom mapping.", indexName);
            } else {
                throw e;
            }
        }
    }
}