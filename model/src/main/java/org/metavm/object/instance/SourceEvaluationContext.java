package org.metavm.object.instance;

import org.metavm.entity.BuiltinKlasses;
import org.metavm.expression.*;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.Types;
import org.metavm.system.RegionConstants;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;

public record SourceEvaluationContext(Source source) implements EvaluationContext {

    @Override
    public Instance evaluate(Expression expression) {
        var thisPropertyExpr = getThisPropertyExpression(expression);
        if (thisPropertyExpr != null) {
            var fieldId = thisPropertyExpr.getProperty().getIdRequired();
            return getSourceField(source, fieldId);
        }
        var equalityExpr = getEqualityExpression(expression);
        if (equalityExpr != null) {
            var left = equalityExpr.getLeft().evaluate(this);
            var right = equalityExpr.getRight().evaluate(this);
            return checkEquals(left, right);
        }
        var inExpr = getInExpression(expression);
        if (inExpr != null) {
            var left = inExpr.getLeft().evaluate(this);
            var right = (ArrayInstance) inExpr.getRight().evaluate(this);
            return Instances.booleanInstance(arrayContains(right, left));
        }
        throw new InternalException(expression + " is not a context expression of " + this);
    }

    private BooleanInstance checkEquals(Instance left, Instance right) {
        if (left instanceof DurableInstance leftDurable
                && right instanceof DurableInstance rightDurable) {
            if (right instanceof ArrayInstance) {
                var tmp = leftDurable;
                leftDurable = rightDurable;
                rightDurable = tmp;
            }
            if (Objects.equals(leftDurable.tryGetId(), rightDurable.tryGetId()))
                return Instances.trueInstance();
            else if (leftDurable instanceof ArrayInstance array && arrayContains(array, rightDurable))
                return Instances.trueInstance();
            else
                return Instances.falseInstance();
        } else
            return Instances.booleanInstance(left.equals(right));
    }

    private boolean arrayContains(ArrayInstance array, Instance instance) {
        for (var element : array) {
            if (element.equals(instance))
                return true;
            if (element instanceof DurableInstance durableElement && instance instanceof DurableInstance durableInstance) {
                if (Objects.equals(durableElement.tryGetId(), durableInstance.tryGetId()))
                    return true;
            }
        }
        return false;
    }

    private Instance getSourceField(Source source, Id fieldId) {
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
                Types.getAnyArrayType(),
                false,
                null
        );
        array.addAll(elementInstances);
        return array;
    }

    private Instance createPrimitiveInstance(PrimitiveFieldValue primitiveValue) {
        var kind = PrimitiveKind.fromCode(primitiveValue.getPrimitiveKind());
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
            return new ArrayInstance(id, Types.getAnyArrayType(), false, null);
        } else {
            return new ClassInstance(id, BuiltinKlasses.entity.get().getType(), false, null);
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return getThisPropertyExpression(expression) != null || getEqualityExpression(expression) != null
                || getInExpression(expression) != null;
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

    private @Nullable BinaryExpression getInExpression(Expression expression) {
        if (expression instanceof BinaryExpression binaryExpression
                && binaryExpression.getOperator() == BinaryOperator.IN)
            return binaryExpression;
        return null;
    }

}
