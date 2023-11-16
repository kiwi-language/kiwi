package tech.metavm.object.instance.search;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Component;
import tech.metavm.common.Page;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.NncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class InstanceSearchServiceImpl implements InstanceSearchService {

    private static final String INDEX = "instance";

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public Page<Long> search(SearchQuery query) {
        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.routing(query.tenantId() + (query.includeBuiltin() ? ",-1" : ""));
        searchRequest.source(SearchBuilder.build(query));
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long total = response.getHits().getTotalHits().value;
            List<Long> ids = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                ids.add(Long.valueOf(hit.getId()));
            }
            return new Page<>(ids, total);
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    public InstanceSearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public void bulk(long tenantId, List<ClassInstance> toIndex, List<Long> toDelete) {
        BulkRequest bulkRequest = new BulkRequest();
        List<IndexRequest> indexRequests = NncUtils.map(toIndex, instance -> buildIndexRequest(tenantId, instance));
        List<DeleteRequest> deleteRequests = NncUtils.map(toDelete, id -> buildDeleteRequest(tenantId, id));
        indexRequests.forEach(bulkRequest::add);
        deleteRequests.forEach(bulkRequest::add);
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        try {
            var resp = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    private IndexRequest buildIndexRequest(long tenantId, ClassInstance instance) {
        IndexRequest indexRequest = new IndexRequest(INDEX);
        indexRequest.id(instance.getId() + "");
        indexRequest.routing(tenantId + "");
        indexRequest.source(IndexSourceBuilder.buildSource(tenantId, instance));
        return indexRequest;
    }

    private DeleteRequest buildDeleteRequest(long tenantId, long id) {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX);
        deleteRequest.id(id + "");
        deleteRequest.routing(tenantId + "");
        return deleteRequest;
    }

}
