package org.metavm.entity;

import org.metavm.common.Page;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.search.*;
import org.metavm.util.ContextUtil;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EntityQueryService {

    private final InstanceSearchService instanceSearchService;

    public EntityQueryService(InstanceSearchService instanceSearchService) {
        this.instanceSearchService = instanceSearchService;
    }

    public <T extends Entity> Page<T> query(EntityQuery<T> query, IInstanceContext context) {
        var searchQuery = buildSearchQuery(query);
        var idPage = instanceSearchService.search(searchQuery);
        return new Page<>(
                Utils.map(idPage.data(), id -> context.getEntity(query.entityType(), id)),
                idPage.total()
        );
    }

    public <T extends Entity> long count(EntityQuery<T> query, IInstanceContext ignored) {
        return instanceSearchService.count(buildSearchQuery(query));
    }

    private SearchQuery buildSearchQuery(EntityQuery<?> query) {
        var expression = buildCondition(query);
        var type = ModelDefRegistry.getDefContext().getKlass(query.entityType()).getType();
        var typeExpressions = Utils.mapToSet(type.getKlass().getDescendantTypes(), k -> k.getType().toExpression());
        return new SearchQuery(
                ContextUtil.getAppId(),
                typeExpressions,
                expression,
                query.includeBuiltin(),
                query.page(),
                query.pageSize(),
                5
        );
    }

    private <T extends Entity> SearchCondition buildCondition(EntityQuery<T> query) {
        var conditions = new ArrayList<SearchCondition>();
        for (var queryField : query.fields()) {
            var esField = queryField.searchField().getEsField();
            var value  = queryField.value();
            if (value.isArray())
                conditions.add(new InSearchCondition(esField, value.resolveArray().getElements()));
            else
                conditions.add(new MatchSearchCondition(esField, value));
        }
        if (conditions.isEmpty()) return null;
        return conditions.size() == 1 ? conditions.getFirst() : new OrSearchCondition(conditions);
    }

}
