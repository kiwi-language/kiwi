package tech.metavm.object.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionUtil;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

@Component
public class InstanceQueryService {

    @Autowired
    private InstanceSearchService instanceSearchService;

    public Page<Long> query(InstanceQuery query, InstanceContext context) {
        return query(
                query.typeId(),
                buildCondition(query, context),
                query.page(),
                query.pageSize(),
                context
        );
    }

    public <T extends Entity> Page<T> query(Class<T> entityType, InstanceQueryDTO query, EntityContext context) {
        Page<Long> idPage = query(query, context.getInstanceContext());
        return new Page<>(
                NncUtils.map(idPage.data(), id -> context.getEntity(entityType, id)),
                idPage.total()
        );
    }

    public Page<Long> query(InstanceQueryDTO query, InstanceContext context) {
        return query(
                query.typeId(),
                buildCondition(query.typeId(), query.searchText(), context),
                query.page(),
                query.pageSize(),
                context
        );
    }

    public Page<Long> query(long typeId, Expression expression, int page, int pageSize, InstanceContext context) {
        SearchQuery searchQuery = new SearchQuery(
                context.getTenantId(),
                typeId,
                expression,
                page,
                pageSize
        );
        return instanceSearchService.search(searchQuery);
    }

    private Expression buildCondition(InstanceQuery query, InstanceContext context) {
        Expression condition = buildCondition(query.typeId(), query.searchText(), context);
        for (InstanceQueryField queryField : query.fields()) {
            Expression fieldCondition = ExpressionUtil.fieldEq(
                    queryField.field(),
                    ExpressionUtil.constant(queryField.value()/*, context*/)
            );
            condition = condition != null ? ExpressionUtil.and(
                    condition,
                    fieldCondition
            ) : fieldCondition;
        }
        return condition;
    }

    private Expression buildCondition(long typeId, String searchText, InstanceContext context) {
        if(NncUtils.isEmpty(searchText)) {
            return null;
        }
        Type type = context.getEntityContext().getType(typeId);
        Field titleField = type.getTileField();
        if(titleField == null) {
            return null;
        }
        if(titleField.isString()) {
            return ExpressionUtil.or(
                    ExpressionUtil.fieldLike(titleField, searchText),
                    ExpressionUtil.fieldStartsWith(titleField, searchText)
            );
        }
        else {
            return ExpressionUtil.fieldEq(titleField, searchText);
        }
    }

}
