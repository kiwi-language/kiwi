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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.TypeStore;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.ValueFormatter;
import tech.metavm.util.NncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.constant.FieldNames.TENANT_ID;
import static tech.metavm.constant.FieldNames.TYPE_ID;

@Component
public class InstanceSearchService {

    public static final String INDEX = "instance";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private EntityContextFactory contextFactory;

    public Page<Long> search(SearchQuery query) {
        SearchRequest searchRequest = new SearchRequest(INDEX);
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

    public void bulk(long tenantId, List<InstanceDTO> toIndex, List<Long> toDelete) {
        BulkRequest bulkRequest = new BulkRequest();
        List<IndexRequest> indexRequests = NncUtils.map(toIndex, instance -> buildIndexRequest(tenantId, instance));
        List<DeleteRequest> deleteRequests = NncUtils.map(toDelete, id -> buildDeleteRequest(tenantId, id));
        indexRequests.forEach(bulkRequest::add);
        deleteRequests.forEach(bulkRequest::add);
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        try {
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("ElasticSearch Error", e);
        }
    }

    private IndexRequest buildIndexRequest(long tenantId, InstanceDTO instance) {
        IndexRequest indexRequest = new IndexRequest(INDEX);
        indexRequest.id(instance.id() + "");
        indexRequest.routing(tenantId + "");
        indexRequest.source(buildSource(tenantId, instance));
        return indexRequest;
    }

    private Map<String, Object> buildSource(long tenantId, InstanceDTO instance) {
        EntityContext entityContext = contextFactory.newContext();
        Type type = entityContext.getType(instance.typeId());
        Map<String, Object> source = new HashMap<>();
        source.put(TENANT_ID, tenantId);
        source.put(TYPE_ID, instance.typeId());
        Map<Long, InstanceFieldDTO> instanceFieldMap = NncUtils.toMap(instance.fields(), InstanceFieldDTO::fieldId);
        for (Field field : type.getFields()) {
            InstanceFieldDTO instanceField = instanceFieldMap.get(field.getId());
            Object fieldValue = ValueFormatter.parse(instanceField.value(), field.getType());
            source.put(field.getColumnName(), fieldValue);
            if(field.isString()) {
                source.put(field.getColumn().fuzzyName(), fieldValue);
            }
        }
        return source;
    }

    private DeleteRequest buildDeleteRequest(long tenantId, long id) {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX);
        deleteRequest.id(id + "");
        deleteRequest.routing(tenantId + "");
        return deleteRequest;
    }

}
