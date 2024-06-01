package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.DefaultCallContext;
import tech.metavm.expression.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.type.*;
import tech.metavm.object.view.FieldsObjectMapping;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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

    public Page<DurableInstance> query(InstanceQuery query, IEntityContext context) {
        return query(query, context.getInstanceContext(),
                new ContextTypeDefRepository(context));
    }

    public Page<DurableInstance> query(InstanceQuery query,
                                       InstanceRepository instanceRepository,
                                       IndexedTypeDefProvider typeDefProvider) {
        var type = query.klass().getType();
        if (type instanceof ClassType classType && query.sourceMapping() != null) {
            var sourceMapping = query.sourceMapping();
            if (!sourceMapping.getTargetType().equals(type) || !(sourceMapping instanceof FieldsObjectMapping sourceObjectMapping))
                throw new BusinessException(ErrorCode.INVALID_SOURCE_MAPPING);
            var sourceQuery = convertToSourceQuery(query, classType.resolve(), sourceObjectMapping,
                    instanceRepository, typeDefProvider);
            var sourcePage = query(sourceQuery, instanceRepository,
                    typeDefProvider);
            return new Page<>(
                    NncUtils.map(sourcePage.data(),
                            i -> sourceObjectMapping.mapRoot(i, new DefaultCallContext(instanceRepository))),
                    sourcePage.total()
            );
        } else
            return queryPhysical(query, instanceRepository, typeDefProvider, instanceRepository);
    }

    private InstanceQuery convertToSourceQuery(InstanceQuery query, Klass viewType,
                                               FieldsObjectMapping mapping,
                                               InstanceProvider instanceProvider,
                                               IndexedTypeDefProvider typeDefProvider) {
        var sourceType = mapping.getSourceType();
        var fieldMap = new HashMap<Field, Field>();
        for (var fieldMapping : mapping.getFieldMappings()) {
            if (fieldMapping.getSourceField() != null)
                fieldMap.put(fieldMapping.getTargetField(), fieldMapping.getSourceField());
        }
        String convertedExpr;
        if (query.expression() != null) {
            var parsingContext = new TypeParsingContext(instanceProvider, typeDefProvider, viewType);
            var cond = ExpressionParser.parse(query.expression(), parsingContext);
            var convertedCond = (Expression) cond.accept(new CopyVisitor(cond, false) {
                @Override
                public Element visitPropertyExpression(PropertyExpression expression) {
                    var field = (Field) expression.getProperty();
                    var sourceField = fieldMap.get(field);
                    if (sourceField == null)
                        throw new BusinessException(ErrorCode.FIELD_NOT_SEARCHABLE, field.getName());
                    return new PropertyExpression(
                            (Expression) expression.getInstance().accept(this),
                            sourceField.getRef()
                    );
                }

                @Override
                public Element visitThisExpression(ThisExpression expression) {
                    return new ThisExpression(sourceType);
                }
            });
            convertedExpr = convertedCond.build(VarType.NAME);
        } else
            convertedExpr = null;
        var queryFields = NncUtils.map(
                query.fields(),
                queryField -> new InstanceQueryField(
                        requireNonNull(fieldMap.get(queryField.field())),
                        queryField.value(),
                        queryField.min(),
                        queryField.max()
                )
        );
        var searchFields = NncUtils.map(query.searchFields(), f -> requireNonNull(fieldMap.get(f)));
        return new InstanceQuery(
                sourceType.resolve(), query.searchText(), convertedExpr, searchFields,
                query.includeBuiltin(), query.includeSubTypes(), query.page(),
                query.pageSize(), queryFields,
                NncUtils.map(query.createdIds(), id -> ((DefaultViewId) id).getSourceId()),
                NncUtils.map(query.excludedIds(), id -> ((DefaultViewId) id).getSourceId()),
                null
        );
    }

    private Page<DurableInstance> queryPhysical(InstanceQuery query,
                                                InstanceRepository instanceRepository,
                                                IndexedTypeDefProvider typeDefProvider,
                                                InstanceProvider instanceProvider) {
        var searchQuery = buildSearchQuery(query, typeDefProvider, instanceProvider);
        var idPage = instanceSearchService.search(searchQuery);
//        var newlyCreatedIds = NncUtils.map(query.createdIds(), id -> ((PhysicalId) id).getId());
//        var excludedIds = NncUtils.mapUnique(query.excludedIds(), id -> ((PhysicalId) id).getId());
        var created = NncUtils.map(query.createdIds(), instanceProvider::get);
        var filteredCreatedId =
                NncUtils.filterAndMap(created, i -> searchQuery.match((ClassInstance) i), DurableInstance::tryGetId);
        List<Id> ids = NncUtils.merge(idPage.data(), filteredCreatedId, true);
        ids = NncUtils.filter(ids, id -> !query.excludedIds().contains(id));
        ids = instanceRepository.filterAlive(ids);
        int actualSize = ids.size();
        ids = ids.subList(0, Math.min(ids.size(), query.pageSize()));
        long total = idPage.total() + (actualSize - idPage.data().size());
        return new Page<>(NncUtils.map(ids, instanceRepository::get), total);
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
                if (queryField.value() instanceof ArrayInstance array) {
                    fieldCondition = Expressions.fieldIn(queryField.field(), array.getElements());
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
        PrimitiveInstance searchTextInst = Instances.stringInstance(searchText);
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
