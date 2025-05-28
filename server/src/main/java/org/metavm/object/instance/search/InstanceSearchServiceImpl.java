package org.metavm.object.instance.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.search.SearchHit;
import org.metavm.common.Page;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.Hooks;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class InstanceSearchServiceImpl implements InstanceSearchService {

    private static final String INDEX = "instance";

    private final RestHighLevelClient restHighLevelClient;

    public InstanceSearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        Hooks.SEARCH_BULK = this::bulk;
    }

    @Override
    public Page<Id> search(SearchQuery query) {
        SearchRequest searchRequest = new SearchRequest(INDEX);
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
        CountRequest countRequest = new CountRequest(INDEX);
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
    public void bulk(long appId, List<ClassInstance> toIndex, List<Id> toDelete) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        List<IndexRequest> indexRequests = Utils.map(toIndex, instance -> buildIndexRequest(appId, instance));
        List<DeleteRequest> deleteRequests = Utils.map(toDelete, id -> buildDeleteRequest(appId, id));
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

    private IndexRequest buildIndexRequest(long appId, ClassInstance instance) {
        IndexRequest indexRequest = new IndexRequest(INDEX);
        indexRequest.id(instance.getTreeId() + "");
        indexRequest.routing(appId + "");
        indexRequest.source(IndexSourceBuilder.buildSource(appId, instance));
        return indexRequest;
    }

    private DeleteRequest buildDeleteRequest(long appId, Id id) {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX);
        deleteRequest.id(Long.toString(id.getTreeId()));
        deleteRequest.routing(appId + "");
        return deleteRequest;
    }

}
