package tech.metavm.object.instance;

import tech.metavm.common.Page;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.*;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.system.RegionConstants;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.MultiApplicationMap;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.util.Constants.ROOT_APP_ID;
import static tech.metavm.util.ContextUtil.getAppId;

public class MemInstanceSearchServiceV2 implements InstanceSearchService {

    private final MultiApplicationMap<Long, Source> sourceMap = new MultiApplicationMap<>();

    @Override
    public Page<Long> search(SearchQuery query) {
        List<Long> result = new ArrayList<>();
        doSearch(query.appId(), query, result);
        if (query.includeBuiltin())
            doSearch(ROOT_APP_ID, query, result);
        Collections.sort(result);
        return new Page<>(
                getPage(result, query.from(), query.end()),
                result.size()
        );
    }

    @Override
    public long count(SearchQuery query) {
        List<Long> result = new ArrayList<>();
        doSearch(query.appId(), query, result);
        if (query.includeBuiltin())
            doSearch(ROOT_APP_ID, query, result);
        return result.size();
    }

    private void doSearch(long appId, SearchQuery query, List<Long> result) {
        Collection<Source> sources = sourceMap.values(appId);
        for (var source : sources) {
            if (match(source, query))
                result.add(source.id);
        }
    }

    private static <T> List<T> getPage(List<T> result, int start, int end) {
        if (start >= result.size())
            return List.of();
        return result.subList(start, Math.min(end, result.size()));
    }

    private boolean match(Source source, SearchQuery query) {
        if (!query.typeIds().contains(source.typeId()))
            return false;
        return query.condition() == null || Instances.isTrue(
                query.condition().evaluate(new SourceEvaluationContext(source, null))
        );
    }


    public boolean contains(long id) {
        return sourceMap.containsKey(getAppId(), id)
                || sourceMap.containsKey(ROOT_APP_ID, id);
    }

    public void add(long appId, ClassInstance instance) {
        bulk(appId, List.of(instance), List.of());
    }

    public void clear() {
        sourceMap.clear();
    }

    private record SourceEvaluationContext(Source source,
                                           ParameterizedFlowProvider parameterizedFlowProvider) implements EvaluationContext {

        @Override
        public Instance evaluate(Expression expression) {
            var thisPropertyExpr = getThisPropertyExpression(expression);
            if (thisPropertyExpr != null) {
                var fieldId = thisPropertyExpr.getProperty().getId();
                return getSourceField(source, fieldId);
            }
            var equalityExpr = getEqualityExpression(expression);
            if (equalityExpr != null) {
                var left = equalityExpr.getLeft().evaluate(this);
                var right = equalityExpr.getRight().evaluate(this);
                if (left instanceof DurableInstance leftDurable
                        && right instanceof DurableInstance rightDurable) {
                    if (right instanceof ArrayInstance) {
                        var tmp = leftDurable;
                        leftDurable = rightDurable;
                        rightDurable = tmp;
                    }
                    if (Objects.equals(leftDurable.tryGetPhysicalId(), rightDurable.tryGetPhysicalId()))
                        return Instances.trueInstance();
                    else if (leftDurable instanceof ArrayInstance array && arrayContains(array, rightDurable))
                        return Instances.trueInstance();
                    else
                        return Instances.falseInstance();
                } else
                    return Instances.booleanInstance(left.equals(right));
            }
            throw new InternalException(expression + " is not a context expression of " + this);
        }

        private boolean arrayContains(ArrayInstance array, Instance instance) {
            for (var element : array) {
                if (element.equals(instance))
                    return true;
                if (element instanceof DurableInstance durableElement && instance instanceof DurableInstance durableInstance) {
                    if (Objects.equals(durableElement.tryGetPhysicalId(), durableInstance.tryGetPhysicalId()))
                        return true;
                }
            }
            return false;
        }

        private Instance getSourceField(Source source, long fieldId) {
            return createInstance(source.fields().get(fieldId));
        }

        private Instance createInstance(FieldValue value) {
            return switch (value) {
                case ReferenceFieldValue refValue -> createReferenceProxy(refValue.getId());
                case PrimitiveFieldValue primitiveValue -> createPrimitiveInstance(primitiveValue);
                case InstanceFieldValue instanceFieldValue
                        when instanceFieldValue.getInstance().param() instanceof ArrayInstanceParam ->
                        createArrayInstance(instanceFieldValue.getInstance());
                default -> throw new IllegalStateException("Unexpected value: " + value);
            };
        }

        private ArrayInstance createArrayInstance(InstanceDTO instanceDTO) {
            var arrayParam = (ArrayInstanceParam) instanceDTO.param();
            var elementInstances = new ArrayList<Instance>();
            for (var elementValue : arrayParam.elements()) {
                var elementInstance = createInstance(elementValue);
                elementInstances.add(elementInstance);
            }
            var array = new ArrayInstance(
                    Id.parse(instanceDTO.id()),
                    StandardTypes.getAnyArrayType(),
                    false,
                    null
            );
            array.addAll(elementInstances);
            return array;
        }

        private Instance createPrimitiveInstance(PrimitiveFieldValue primitiveValue) {
            var kind = PrimitiveKind.getByCode(primitiveValue.getPrimitiveKind());
            return switch (kind) {
                case PASSWORD -> Instances.passwordInstance((String) primitiveValue.getValue());
                case NULL -> Instances.nullInstance();
                case BOOLEAN -> Instances.booleanInstance((Boolean) primitiveValue.getValue());
                case STRING -> Instances.stringInstance((String) primitiveValue.getValue());
                case LONG -> Instances.longInstance((Long) primitiveValue.getValue());
                case DOUBLE -> Instances.doubleInstance((Double) primitiveValue.getValue());
                case TIME -> Instances.timeInstance((Long) primitiveValue.getValue());
                case VOID -> throw new InternalException("Can not create a void instance");
            };
        }

        private DurableInstance createReferenceProxy(String idStr) {
            var id = Id.parse(idStr);
            if (RegionConstants.isArrayId(id)) {
                return new ArrayInstance(id, StandardTypes.getAnyArrayType(), false, null);
            } else {
                return new ClassInstance(id, StandardTypes.getEntityType(), false, null);
            }
        }

        @Override
        public boolean isContextExpression(Expression expression) {
            return getThisPropertyExpression(expression) != null || getEqualityExpression(expression) != null;
        }

        private @Nullable PropertyExpression getThisPropertyExpression(Expression expression) {
            if (expression instanceof PropertyExpression propertyExpression
                    && propertyExpression.getInstance() instanceof ThisExpression)
                return propertyExpression;
            else
                return null;
        }

        private @Nullable BinaryExpression getEqualityExpression(Expression expression) {
            if (expression instanceof BinaryExpression binaryExpression
                    && binaryExpression.getOperator() == BinaryOperator.EQ)
                return binaryExpression;
            else
                return null;
        }
    }

    @Override
    public void bulk(long appId, List<ClassInstance> toIndex, List<Long> toDelete) {
        for (ClassInstance instance : toIndex) {
            NncUtils.requireNonNull(instance.tryGetPhysicalId());
            sourceMap.put(
                    appId,
                    instance.tryGetPhysicalId(),
                    buildSource(instance)
            );
        }
        for (Long id : toDelete) {
            sourceMap.remove(appId, id);
            sourceMap.remove(appId, id);
        }
    }

    private Source buildSource(ClassInstance instance) {
        var fields = new HashMap<Long, FieldValue>();
        instance.forEachField((field, value) -> {
            if (!field.isChild())
                fields.put(field.getId(), value.toFieldValueDTO());
        });
        return new Source(
                instance.getPhysicalId(),
                instance.getType().getId(),
                fields
        );
    }

    public MemInstanceSearchServiceV2 copy() {
        var copy = new MemInstanceSearchServiceV2();
        copy.sourceMap.putAll(sourceMap);
        return copy;
    }

    private record Source(long id, long typeId, Map<Long, FieldValue> fields) {
    }

}
