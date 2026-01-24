package org.manul.object.instance;

import org.manul.common.ErrorCode;
import org.manul.common.Page;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.entity.InstanceQuery;
import org.manul.entity.InstanceQueryField;
import org.manul.expression.*;
import org.manul.object.instance.core.*;
import org.manul.object.instance.search.*;
import org.manul.object.type.*;
import org.manul.util.BusinessException;
import org.manul.util.ContextUtil;
import org.manul.util.Instances;
import org.manul.util.Utils;
import org.manul.context.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InstanceQueryService {

    private final InstanceSearchService instanceSearchService;

    public InstanceQueryService(InstanceSearchService instanceSearchService) {
        this.instanceSearchService = instanceSearchService;
    }

    private SearchQuery buildSearchQuery(InstanceQuery query,
                                         Expression condition) {
        var type = query.klass().getType();
        Set<String> typeExpressions = (type instanceof KlassType classType) ?
                Utils.mapToSet(classType.getKlass().getDescendantTypes(), k -> k.getType().toExpression()) :
                Set.of(type.toExpression());
        var cond = Utils.safeCall(condition, SearchBuilder::buildSearchCondition);
        if (!query.ids().isEmpty()) {
            var idCond = new IdSearchCondition(new HashSet<>(query.ids()));
            if (cond == null)
                cond = idCond;
            else
                cond = new AndSearchCondition(List.of(idCond, cond));
        }
        return new SearchQuery(
                ContextUtil.getAppId(),
                typeExpressions,
                cond,
                query.includeBuiltin(),
                query.page(),
                query.pageSize(),
                5 + query.excludedIds().size()
        );
    }

    public Page<Reference> query(InstanceQuery query, IInstanceContext context) {
        return query(query, context,
                new ContextTypeDefRepository(context));
    }

    public Page<Reference> query(InstanceQuery query,
                                 InstanceRepository instanceRepository,
                                 IndexedTypeDefProvider typeDefProvider) {
        var condition = buildCondition(query, typeDefProvider, instanceRepository);
        var searchQuery = buildSearchQuery(query, condition);
        var idPage = instanceSearchService.search(searchQuery);
//        var newlyCreatedIds = NncUtils.map(query.createdIds(), id -> ((PhysicalId) id).getId());
//        var excludedIds = NncUtils.mapUnique(query.excludedIds(), id -> ((PhysicalId) id).getId());
        var created = Utils.map(query.createdIds(), instanceRepository::get);
        var filteredCreatedId =
                Utils.filterAndMap(created, i -> match((ClassInstance) i, searchQuery), Instance::tryGetId);
        List<Id> ids = Utils.merge(idPage.items(), filteredCreatedId, true);
        ids = Utils.filter(ids, id -> !query.excludedIds().contains(id));
        ids = instanceRepository.filterAlive(ids);
        int actualSize = ids.size();
        ids = ids.subList(0, Math.min(ids.size(), query.pageSize()));
        long total = idPage.total() + (actualSize - idPage.items().size());
        return new Page<>(Utils.map(ids, id -> instanceRepository.get(id).getReference()), total);
    }

    private boolean match(ClassInstance instance, SearchQuery query) {
        return query.types().contains(instance.getInstanceType().toExpression()) &&
                (query.condition() == null || query.condition().evaluate(instance.getId(), instance.buildSource()));
    }

    public long count(InstanceQuery query, IInstanceContext context) {
        return instanceSearchService.count(buildSearchQuery(query,
                buildCondition(query, new ContextTypeDefRepository(context), context)));
    }

    private Expression buildCondition(InstanceQuery query,
                                      IndexedTypeDefProvider typeDefProvider,
                                      InstanceProvider instanceProvider) {
        Expression condition = buildConditionForSearchText(
                query.klass(), query.searchText(), query.searchFields(), typeDefProvider
        );
        for (InstanceQueryField queryField : query.fields()) {
            Expression fieldCondition = null;
            if (queryField.value() != null) {
                if (queryField.value().isArray()) {
                    fieldCondition = Expressions.fieldIn(queryField.field(), queryField.value().resolveArray().getElements());
                } else {
                    fieldCondition = Expressions.fieldEq(queryField.field(), queryField.value());
                }
            } else {
                if (queryField.min() != null) {
                    fieldCondition = Expressions.ge(
                            Expressions.propertyExpr(queryField.field()),
                            new ConstantExpression(queryField.min()));
                }
                if (queryField.max() != null) {
                    var leExpr = Expressions.le(
                            Expressions.propertyExpr(queryField.field()),
                            new ConstantExpression(queryField.max())
                    );
                    fieldCondition = fieldCondition != null ?
                            Expressions.and(fieldCondition, leExpr) : leExpr;
                }
            }
            if (fieldCondition == null)
                throw new BusinessException(ErrorCode.ILLEGAL_SEARCH_CONDITION);
            condition = condition != null ?
                    Expressions.and(condition, fieldCondition) : fieldCondition;
        }
        if (query.expression() != null) {
            var parsingContext = new TypeParsingContext(instanceProvider, typeDefProvider, query.klass());
            var exprCond = ExpressionParser.parse(query.expression(), parsingContext);
            condition = condition != null ? Expressions.and(condition, exprCond) : exprCond;
        }
        return condition;
    }

    private Expression buildConditionForSearchText(Klass klass, String searchText,
                                                   List<Field> searchFields,
                                                   TypeDefProvider typeDefProvider) {
        if (Utils.isEmpty(searchText))
            return null;
        Set<Field> searchFieldSet = new HashSet<>(searchFields);
        Field titleField = klass.getTitleField();
        if (titleField != null && !searchFields.contains(titleField))
            searchFieldSet.add(titleField);
        if (searchFieldSet.isEmpty())
            return null;
        var searchTextInst = Instances.stringInstance(searchText);
        Expression result = null;
        for (Field field : searchFieldSet) {
            Expression expression;
            if (field.isString()) {
                expression = Expressions.or(
                        Expressions.fieldLike(field, searchTextInst),
                        Expressions.fieldStartsWith(field, searchTextInst)
                );
            } else
                expression = Expressions.fieldEq(field, searchTextInst);
            if (result == null)
                result = expression;
            else
                result = Expressions.or(result, expression);
        }
        return result;
    }

}
