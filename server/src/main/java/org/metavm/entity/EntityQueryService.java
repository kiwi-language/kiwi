package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.Page;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.StringReference;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.search.*;
import org.metavm.util.ContextUtil;
import org.metavm.util.Utils;
import org.metavm.context.Component;

import java.util.ArrayList;
import java.util.HashSet;

@Slf4j
@Component
public class EntityQueryService {

    private final InstanceSearchService instanceSearchService;

    public EntityQueryService(InstanceSearchService instanceSearchService) {
        this.instanceSearchService = instanceSearchService;
    }

    public <T extends Entity> Page<T> query(EntityQuery<T> query, IInstanceContext context) {
        var searchQuery = buildSearchQuery(query);
        var idPage = instanceSearchService.search(searchQuery);
        var ids = idPage.items();
        var idSet = new HashSet<>(ids);
        var aliveIds = new HashSet<>(context.filterAlive(ids));
        var items = new ArrayList<T>();
        var total = idPage.total();
        var cond = searchQuery.condition();
        for (Id id : query.newlyCreatedIds()) {
            if (idSet.contains(id))
                continue;
            var item = context.getEntity(query.entityType(), id);
            if (cond == null || cond.evaluate(item.buildSource())) {
                if (items.size() < query.pageSize())
                    items.add(item);
                total++;
            }
        }
        for (Id id : ids) {
            if (aliveIds.contains(id)) {
                var item = context.getEntity(query.entityType(), id);
                if (cond == null || cond.evaluate(item.buildSource())) {
                    if (items.size() < query.pageSize())
                        items.add(item);
                } else
                    total--;
            }
            else
                total--;
        }
        return new Page<>(items, total);
    }

    public <T extends Entity> long count(EntityQuery<T> query, IInstanceContext ignored) {
        return instanceSearchService.count(buildSearchQuery(query));
    }

    private SearchQuery buildSearchQuery(EntityQuery<?> query) {
        var expression = buildCondition(query);
        var type = StdKlassRegistry.instance.getKlass(query.entityType()).getType();
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
            var cond = switch (queryField.op()) {
                case MATCH -> buildSearchCond(value, esField);
                case NOT_MATCH -> new NotSearchCondition(buildSearchCond(value, esField));
            };
            conditions.add(cond);
        }
        if (conditions.isEmpty()) return null;
        return conditions.size() == 1 ? conditions.getFirst() : new AndSearchCondition(conditions);
    }

    private SearchCondition buildSearchCond(Value value, String esField) {
        if (value.isArray())
            return new InSearchCondition(esField, value.resolveArray().getElements());
        else if (value instanceof StringReference s && !SearchUtil.containsDelimiter(s.getValue()))
            return new PrefixSearchCondition(esField, s);
        else
            return new MatchSearchCondition(esField, value);
    }

}
