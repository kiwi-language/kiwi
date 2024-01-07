package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.expression.*;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.type.*;
import tech.metavm.object.view.FieldsObjectMapping;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Component
public class InstanceQueryService {

    private final InstanceSearchService instanceSearchService;

    public InstanceQueryService(InstanceSearchService instanceSearchService) {
        this.instanceSearchService = instanceSearchService;
    }

    private SearchQuery buildSearchQuery(InstanceQuery query,
                                         IndexedTypeProvider typeProvider,
                                         InstanceProvider instanceProvider,
                                         ArrayTypeProvider arrayTypeProvider) {
        var expression = buildCondition(query, typeProvider, instanceProvider, arrayTypeProvider);
        Type type = query.type();
        Set<Long> typeIds = (type instanceof ClassType classType) ? classType.getSubTypeIds() :
                Set.of(query.type().tryGetId());
        return new SearchQuery(
                ContextUtil.getAppId(),
                typeIds,
                expression,
                query.includeBuiltin(),
                query.page(),
                query.pageSize(),
                5 + query.excludedIds().size()
        );
    }

    public Page<DurableInstance> query(InstanceQuery query, IEntityContext context) {
        return query(query, context.getInstanceContext(), context.getGenericContext(),
                new ContextTypeRepository(context), new ContextArrayTypeProvider(context));
    }

    public Page<DurableInstance> query(InstanceQuery query,
                                       InstanceRepository instanceRepository,
                                       ParameterizedFlowProvider parameterizedFlowProvider,
                                       IndexedTypeProvider typeProvider,
                                       ArrayTypeProvider arrayTypeProvider) {
        var type = query.type();
        if (type instanceof ClassType classType && query.sourceMapping() != null) {
            var sourceMapping = query.sourceMapping();
            if (sourceMapping.getTargetType() != type || !(sourceMapping instanceof FieldsObjectMapping sourceObjectMapping))
                throw new BusinessException(ErrorCode.INVALID_SOURCE_MAPPING);
            var sourceQuery = convertToSourceQuery(query, classType, sourceObjectMapping,
                    instanceRepository, typeProvider, arrayTypeProvider);
            var sourcePage = query(sourceQuery, instanceRepository, parameterizedFlowProvider,
                    typeProvider, arrayTypeProvider);
            return new Page<>(
                    NncUtils.map(sourcePage.data(),
                            i -> sourceObjectMapping.mapRoot(i, instanceRepository, parameterizedFlowProvider)),
                    sourcePage.total()
            );
        } else
            return queryPhysical(query, instanceRepository, typeProvider, instanceRepository, arrayTypeProvider);
    }

    private InstanceQuery convertToSourceQuery(InstanceQuery query, ClassType viewType,
                                               FieldsObjectMapping mapping,
                                               InstanceProvider instanceProvider,
                                               IndexedTypeProvider typeProvider,
                                               ArrayTypeProvider arrayTypeProvider) {
        var sourceType = mapping.getSourceType();
        var fieldMap = new HashMap<Field, Field>();
        for (var fieldMapping : mapping.getFieldMappings()) {
            if (fieldMapping.getSourceField() != null)
                fieldMap.put(fieldMapping.getTargetField(), fieldMapping.getSourceField());
        }
        String convertedExpr;
        if (query.expression() != null) {
            var parsingContext = new TypeParsingContext(instanceProvider, typeProvider, arrayTypeProvider, viewType);
            var cond = ExpressionParser.parse(query.expression(), parsingContext);
            var convertedCond = (Expression) cond.accept(new CopyVisitor(cond) {
                @Override
                public Element visitPropertyExpression(PropertyExpression expression) {
                    var field = (Field) expression.getProperty();
                    var sourceField = fieldMap.get(field);
                    if (sourceField == null)
                        throw new BusinessException(ErrorCode.FIELD_NOT_SEARCHABLE, field.getName());
                    return new PropertyExpression(
                            (Expression) expression.getInstance().accept(this),
                            sourceField
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
                sourceType, query.searchText(), convertedExpr, searchFields,
                query.includeBuiltin(), query.includeSubTypes(), query.page(),
                query.pageSize(), queryFields,
                NncUtils.map(query.createdIds(), id -> ((ViewId) id).getSourceId()),
                NncUtils.map(query.excludedIds(), id -> ((ViewId) id).getSourceId()),
                null
        );
    }

    private Page<DurableInstance> queryPhysical(InstanceQuery query,
                                                InstanceRepository instanceRepository,
                                                IndexedTypeProvider typeProvider,
                                                InstanceProvider instanceProvider,
                                                ArrayTypeProvider arrayTypeProvider) {
        var idPage = instanceSearchService.search(buildSearchQuery(query, typeProvider, instanceProvider, arrayTypeProvider));
        var newlyCreatedIds = NncUtils.map(query.createdIds(), id -> ((PhysicalId) id).getId());
        var excludedIds = NncUtils.mapUnique(query.excludedIds(), id -> ((PhysicalId) id).getId());
        List<Long> ids = NncUtils.merge(idPage.data(), newlyCreatedIds, true);
        ids = NncUtils.filter(ids, id -> !excludedIds.contains(id));
        ids = instanceRepository.filterAlive(ids);
        int actualSize = ids.size();
        ids = ids.subList(0, Math.min(ids.size(), query.pageSize()));
        long total = idPage.total() + (actualSize - idPage.data().size());
        return new Page<>(NncUtils.map(ids, id -> instanceRepository.get(PhysicalId.of(id))), total);
    }

    public long count(InstanceQuery query, IEntityContext context) {
        return instanceSearchService.count(buildSearchQuery(query,
                new ContextTypeRepository(context),
                context.getInstanceContext(),
                new ContextArrayTypeProvider(context)));
    }

    private Expression buildCondition(InstanceQuery query,
                                      IndexedTypeProvider typeProvider,
                                      InstanceProvider instanceProvider,
                                      ArrayTypeProvider arrayTypeProvider) {
        Expression condition = buildConditionForSearchText(
                query.type().tryGetId(), query.searchText(), query.searchFields(), typeProvider
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
            var parsingContext = new TypeParsingContext(instanceProvider, typeProvider, arrayTypeProvider, (ClassType) query.type());
            var exprCond = ExpressionParser.parse(query.expression(), parsingContext);
            condition = condition != null ? Expressions.and(condition, exprCond) : exprCond;
        }
        return condition;
    }

    private Expression buildConditionForSearchText(long typeId, String searchText,
                                                   List<Field> searchFields,
                                                   TypeProvider typeProvider) {
        if (NncUtils.isEmpty(searchText))
            return null;
        Set<Field> searchFieldSet = new HashSet<>(searchFields);
        ClassType type = typeProvider.getClassType(typeId);
        Field titleField = type.getTitleField();
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
