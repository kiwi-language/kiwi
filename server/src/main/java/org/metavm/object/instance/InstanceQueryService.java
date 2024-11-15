package org.metavm.object.instance;

import org.metavm.common.ErrorCode;
import org.metavm.common.Page;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.InstanceQuery;
import org.metavm.entity.InstanceQueryField;
import org.metavm.expression.*;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.instance.search.SearchQuery;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;

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
                                         IndexedTypeDefProvider typeDefProvider,
                                         InstanceProvider instanceProvider) {
        var expression = buildCondition(query, typeDefProvider, instanceProvider);
        var type = query.klass().getType();
        Set<String> typeExpressions = (type instanceof ClassType classType) ?
                NncUtils.mapUnique(classType.resolve().getDescendantTypes(), k -> k.getType().toExpression()) :
                Set.of(type.toExpression());
        return new SearchQuery(
                ContextUtil.getAppId(),
                typeExpressions,
                expression,
                query.includeBuiltin(),
                query.page(),
                query.pageSize(),
                5 + query.excludedIds().size()
        );
    }

    public Page<Reference> query(InstanceQuery query, IEntityContext context) {
        return query(query, context.getInstanceContext(),
                new ContextTypeDefRepository(context));
    }

    public Page<Reference> query(InstanceQuery query,
                                 InstanceRepository instanceRepository,
                                 IndexedTypeDefProvider typeDefProvider) {
        var searchQuery = buildSearchQuery(query, typeDefProvider, instanceRepository);
        var idPage = instanceSearchService.search(searchQuery);
//        var newlyCreatedIds = NncUtils.map(query.createdIds(), id -> ((PhysicalId) id).getId());
//        var excludedIds = NncUtils.mapUnique(query.excludedIds(), id -> ((PhysicalId) id).getId());
        var created = NncUtils.map(query.createdIds(), instanceRepository::get);
        var filteredCreatedId =
                NncUtils.filterAndMap(created, i -> searchQuery.match((ClassInstance) i), Instance::tryGetId);
        List<Id> ids = NncUtils.merge(idPage.data(), filteredCreatedId, true);
        ids = NncUtils.filter(ids, id -> !query.excludedIds().contains(id));
        ids = instanceRepository.filterAlive(ids);
        int actualSize = ids.size();
        ids = ids.subList(0, Math.min(ids.size(), query.pageSize()));
        long total = idPage.total() + (actualSize - idPage.data().size());
        return new Page<>(NncUtils.map(ids, id -> instanceRepository.get(id).getReference()), total);
    }

    public long count(InstanceQuery query, IEntityContext context) {
        return instanceSearchService.count(buildSearchQuery(query,
                new ContextTypeDefRepository(context),
                context.getInstanceContext())
        );
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
        if (NncUtils.isEmpty(searchText))
            return null;
        Set<Field> searchFieldSet = new HashSet<>(searchFields);
        Field titleField = klass.getTitleField();
        if (titleField != null && !searchFields.contains(titleField))
            searchFieldSet.add(titleField);
        if (searchFieldSet.isEmpty())
            return null;
        PrimitiveValue searchTextInst = Instances.stringInstance(searchText);
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
