package tech.metavm.object.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class InstanceQueryService {

    @Autowired
    private EntityContextFactory contextFactory;

    @Autowired
    private InstanceSearchService instanceSearchService;


    public Page<Long> query(long tenantId, InstanceQueryDTO query) {
        SearchQuery searchQuery = new SearchQuery(
                tenantId,
                query.typeId(),
                buildCondition(tenantId, query),
                query.page(),
                query.pageSize()
        );
        return instanceSearchService.search(searchQuery);
    }

    private Expression buildCondition(long tenantId, InstanceQueryDTO queryDTO) {
        if(NncUtils.isEmpty(queryDTO.searchText())) {
            return null;
        }
        Type type = contextFactory.newContext(tenantId).getType(queryDTO.typeId());
        Field titleField = type.getTileField();
        if(titleField == null) {
            return null;
        }
        if(titleField.isString()) {
            return Expression.or(
                    Expression.fieldLike(titleField, queryDTO.searchText()),
                    Expression.fieldStartsWith(titleField, queryDTO.searchText())
            );
        }
        else {
            return Expression.fieldEq(titleField, queryDTO.searchText());
        }
    }

    public Page<Long> query(long typeId, Expression expression, int page, int pageSize) {
        return new Page<>(List.of(), 0L);
    }

}
