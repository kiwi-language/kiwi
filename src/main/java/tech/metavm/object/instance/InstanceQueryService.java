package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceQuery;
import tech.metavm.entity.InstanceQueryField;
import tech.metavm.expression.ConstantExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InstanceQueryService {

    private final InstanceSearchService instanceSearchService;

    public InstanceQueryService(InstanceSearchService instanceSearchService) {
        this.instanceSearchService = instanceSearchService;
    }

    public <T extends Entity> Page<T> query(Class<T> entityType, InstanceQuery query, IEntityContext context) {
        Page<Instance> idPage = query(query, context.getInstanceContext());
        return new Page<>(
                NncUtils.map(idPage.data(), inst -> context.getEntity(entityType, inst)),
                idPage.total()
        );
    }

    public Page<Instance> query(InstanceQuery query, IInstanceContext context) {
        var expression = buildCondition(query, context);
        Type type = query.type();
        Set<Long> typeIds = (type instanceof ClassType classType) ? classType.getSubTypeIds() :
                Set.of(query.type().getIdRequired());
        SearchQuery searchQuery = new SearchQuery(
                context.getTenantId(),
                typeIds,
                expression,
                query.includeBuiltin(),
                query.page(),
                query.pageSize() + 5
        );
        var idPage = instanceSearchService.search(searchQuery);
        List<Long> ids = NncUtils.merge(idPage.data(), query.newlyCreated(), true);
        ids = context.filterAlive(ids);
        ids = ids.subList(0, Math.min(ids.size(), query.pageSize()));
        long total = idPage.total() + (ids.size() - idPage.data().size());
        return new Page<>(NncUtils.map(ids, context::get), total);
    }

    private Expression buildCondition(InstanceQuery query, IInstanceContext context) {
        Expression condition = buildConditionForSearchText(
                query.type().getIdRequired(), query.searchText(), query.searchFields(), context
        );
        for (InstanceQueryField queryField : query.fields()) {
            Expression fieldCondition = null;
            if(queryField.value() != null) {
                if (queryField.value() instanceof ArrayInstance array) {
                    fieldCondition = ExpressionUtil.fieldIn(queryField.field(), array.getElements());
                } else {
                    fieldCondition = ExpressionUtil.fieldEq(queryField.field(), queryField.value());
                }
            }
            else {
                if(queryField.min() != null) {
                    fieldCondition = ExpressionUtil.ge(
                            ExpressionUtil.propertyExpr(queryField.field()),
                            new ConstantExpression(queryField.min()));
                }
                if(queryField.max() != null) {
                    var leExpr = ExpressionUtil.le(
                            ExpressionUtil.propertyExpr(queryField.field()),
                            new ConstantExpression(queryField.max())
                    );
                    fieldCondition = fieldCondition != null ?
                            ExpressionUtil.and(fieldCondition, leExpr) : leExpr;
                }
            }
            if(fieldCondition == null)
                throw new BusinessException(ErrorCode.ILLEGAL_SEARCH_CONDITION);
            condition = condition != null ?
                    ExpressionUtil.and(condition, fieldCondition) : fieldCondition;
        }
        if(query.expression() != null) {
            var exprCond = ExpressionParser.parse((ClassType) query.type(), query.expression(), context);
            condition = condition != null ? ExpressionUtil.and(condition, exprCond) : exprCond;
        }
        return condition;
    }

    private Expression buildConditionForSearchText(long typeId, String searchText,
                                                   List<Field> searchFields, IInstanceContext context) {
        if (NncUtils.isEmpty(searchText))
            return null;
        Set<Field> searchFieldSet = new HashSet<>(searchFields);
        ClassType type = context.getEntityContext().getClassType(typeId);
        Field titleField = type.getTileField();
        if (titleField != null && !searchFields.contains(titleField))
            searchFieldSet.add(titleField);
        if (searchFieldSet.isEmpty())
            return null;
        PrimitiveInstance searchTextInst = InstanceUtils.stringInstance(searchText);
        Expression result = null;
        for (Field field : searchFieldSet) {
            Expression expression;
            if (field.isString()) {
                expression = ExpressionUtil.or(
                        ExpressionUtil.fieldLike(field, searchTextInst),
                        ExpressionUtil.fieldStartsWith(field, searchTextInst)
                );
            } else {
                expression = ExpressionUtil.fieldEq(field, searchTextInst);
            }
            if (result == null) {
                result = expression;
            } else {
                result = ExpressionUtil.or(result, expression);
            }
        }
        return result;
    }

}
