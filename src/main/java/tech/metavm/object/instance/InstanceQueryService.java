package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionUtil;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

import static tech.metavm.util.InstanceUtils.resolveValue;

@Component
public class InstanceQueryService {

    private final InstanceSearchService instanceSearchService;

    public InstanceQueryService(InstanceSearchService instanceSearchService) {
        this.instanceSearchService = instanceSearchService;
    }

    public Page<Long> query(InstanceQuery query, IInstanceContext context) {
        return query(
                query.typeId(),
                buildCondition(query, context),
                query.page(),
                query.pageSize(),
                context
        );
    }

    public <T extends Entity> Page<T> query(Class<T> entityType, InstanceQueryDTO query, IEntityContext context) {
        Page<Long> idPage = query(query, context.getInstanceContext());
        return new Page<>(
                NncUtils.map(idPage.data(), id -> context.getEntity(entityType, id)),
                idPage.total()
        );
    }

    public Page<Long> query(InstanceQueryDTO query, IInstanceContext context) {
        return query(
                query.typeId(),
                buildConditionForSearchText(query.typeId(), query.searchText(), context),
                query.page(),
                query.pageSize(),
                context
        );
    }

    public Page<Long> query(long typeId, Expression expression, int page, int pageSize, IInstanceContext context) {
        SearchQuery searchQuery = new SearchQuery(
                context.getTenantId(),
                typeId,
                expression,
                page,
                pageSize
        );
        return instanceSearchService.search(searchQuery);
    }

    private Expression buildCondition(InstanceQuery query, IInstanceContext context) {
        Expression condition = buildConditionForSearchText(query.typeId(), query.searchText(), context);
        for (InstanceQueryField queryField : query.fields()) {
            Expression fieldCondition;
            if(queryField.value() instanceof Collection<?> values) {
                List<Instance> instanceValues = NncUtils.map(
                    values, v -> resolveValue(queryField.field().getType(), v)
                );
                fieldCondition = ExpressionUtil.fieldIn(queryField.field(), instanceValues);
            }
            else {
                Instance fieldValue =
                        resolveValue(queryField.field().getType(), queryField.value());
                fieldCondition = ExpressionUtil.fieldEq(queryField.field(), fieldValue);
            }
            condition = condition != null ?
                    ExpressionUtil.and(condition, fieldCondition) : fieldCondition;
        }
        return condition;
    }

    private Expression buildConditionForSearchText(long typeId, String searchText, IInstanceContext context) {
        if(NncUtils.isEmpty(searchText)) {
            return null;
        }
        ClassType type = context.getEntityContext().getClassType(typeId);
        Field titleField = type.getTileField();
        PrimitiveInstance searchTextInst = InstanceUtils.stringInstance(searchText);
        if(titleField == null) {
            return null;
        }
        if(titleField.isString()) {
            return ExpressionUtil.or(
                    ExpressionUtil.fieldLike(titleField, searchTextInst),
                    ExpressionUtil.fieldStartsWith(titleField, searchTextInst)
            );
        }
        else {
            return ExpressionUtil.fieldEq(titleField, searchTextInst);
        }
    }

}
