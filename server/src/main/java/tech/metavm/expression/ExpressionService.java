package tech.metavm.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.dto.*;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.flow.ValueKind;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.instance.rest.ExpressionFieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ExpressionService extends EntityContextFactoryBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionService.class);

    public static final int CONTEXT_TYPE_TYPE = 1;

    public static final int CONTEXT_TYPE_FLOW = 2;

    public static final Set<BinaryOperator> SEARCH_EXPR_OPERATORS = Set.of(
            BinaryOperator.EQ, BinaryOperator.IN, BinaryOperator.STARTS_WITH, BinaryOperator.LIKE
    );

    public ExpressionService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public BoolExprDTO parseBoolExpr(BoolExprParseRequest request) {
        try (var context = newContext()) {
            Expression expression = ExpressionParser.parse(
                    extractExpr(request.value()),
                    getParsingContext(request.context(), context)
            );
            if (expression == null) {
                return new BoolExprDTO(List.of());
            }
            return new BoolExprDTO(parseConditionGroups(expression));
        } catch (ExpressionParsingException e) {
            LOGGER.warn("fail to parse expression: " + request.value(), e);
            throw BusinessException.invalidConditionExpr(NncUtils.toJSONString(request.value()));
        }
    }

    public List<InstanceSearchItemDTO> parseSearchText(String typeId, String searchText) {
        try (var entityContext = newContext()) {
            var context = entityContext.getInstanceContext();
            ClassType type = context.getClassType(Id.parse(typeId));
            Expression expression = ExpressionParser.parse(searchText, TypeParsingContext.create(type, entityContext));
            return parseExpression(expression);
        }
    }

    private List<InstanceSearchItemDTO> parseExpression(Expression expression) {
        List<InstanceSearchItemDTO> result = new ArrayList<>();
        parseExpression0(expression, result);
        return result;
    }

    private void parseExpression0(Expression expression, List<InstanceSearchItemDTO> result) {
        if (!(expression instanceof BinaryExpression binaryExpression)) {
            throw BusinessException.invalidExpression(expression.buildSelf(VarType.NAME, false));
        }
        if (binaryExpression.getOperator() == BinaryOperator.AND) {
            parseExpression0(binaryExpression.getLeft(), result);
            parseExpression0(binaryExpression.getRight(), result);
        } else if (binaryExpression.getOperator() == BinaryOperator.OR) {
            parseExpression0(binaryExpression.getLeft(), result);
        } else {
            result.add(parseFieldExpr(binaryExpression));
        }
    }

    private InstanceSearchItemDTO parseFieldExpr(BinaryExpression binaryExpression) {
        Expression first = binaryExpression.getLeft();
        Expression second = binaryExpression.getRight();
        BinaryOperator operator = binaryExpression.getOperator();
        if (!SEARCH_EXPR_OPERATORS.contains(operator)
                || !(first instanceof PropertyExpression fieldExpr)
                || !(second instanceof ConstantExpression constExpr)) {
            throw BusinessException.invalidExpression(binaryExpression.buildSelf(VarType.NAME,false ));
        }
        Object searchValue;
        if (constExpr.getValue() instanceof PrimitiveInstance primitiveInstance)
            searchValue = primitiveInstance.getValue();
        else
            searchValue = NncUtils.requireNonNull(((DurableInstance) constExpr.getValue()).tryGetPhysicalId());
        return new InstanceSearchItemDTO(fieldExpr.getProperty().getIdRequired().getPhysicalId(), searchValue);
    }

    private String extractExpr(ValueDTO value) throws ExpressionParsingException {
        if (value.kind() == ValueKind.CONSTANT.code()) {
            if (value.value() instanceof PrimitiveFieldValue primValue) {
                if (Boolean.TRUE.equals(primValue.getValue())) {
                    return "true";
                }
            } else {
                throw new ExpressionParsingException();
            }
        }
        if (value.kind() == ValueKind.REFERENCE.code()) {
            ExpressionFieldValue exprValue = (ExpressionFieldValue) value.value();
            return exprValue.getExpression().replaceAll("-", ".") + " = true";
        } else {
            return ((ExpressionFieldValue) value.value()).getExpression();
        }
    }

    private ParsingContext getParsingContext(ParsingContextDTO contextDTO, IEntityContext entityContext) {
        try (var context = newContext()) {
            if (contextDTO instanceof FlowParsingContextDTO flowContext) {
                NodeRT prev = NncUtils.get(flowContext.getPrevNodeId(), context::getNode);
                ScopeRT scope = context.getScope(flowContext.getScopeId());
                return FlowParsingContext.create(scope, prev, entityContext);
            } else if (contextDTO instanceof TypeParsingContextDTO typeContext) {
                return TypeParsingContext.create(context.getClassType(typeContext.getTypeId()), entityContext);
            }
            throw BusinessException.invalidParams("请求参数错误，未识别的解析上下文类型: " + contextDTO.getClass().getName());
        }
    }

    private List<ConditionGroupDTO> parseConditionGroups(Expression expression) throws ExpressionParsingException {
        if (expression instanceof BinaryExpression binaryExpression) {
            if (binaryExpression.getOperator() == BinaryOperator.OR) {
                List<ConditionGroupDTO> firstGroups = parseConditionGroups(binaryExpression.getLeft());
                List<ConditionGroupDTO> secondGroups = parseConditionGroups(binaryExpression.getRight());
                return NncUtils.union(firstGroups, secondGroups);
            }
        }
        if (Expressions.isConstantTrue(expression)) {
            return List.of();
        }
        return List.of(new ConditionGroupDTO(parseConditions(expression)));
    }

    private List<ConditionDTO> parseConditions(Expression expression) throws ExpressionParsingException {
        if (expression instanceof BinaryExpression binaryExpression) {
            if (binaryExpression.getOperator() == BinaryOperator.AND) {
                List<ConditionDTO> firstGroups = parseConditions(binaryExpression.getLeft());
                List<ConditionDTO> secondGroups = parseConditions(binaryExpression.getRight());
                return NncUtils.union(firstGroups, secondGroups);
            }
        }
        if (Expressions.isConstantTrue(expression)) {
            return List.of();
        }
        return List.of(parseSingleCondition(expression));
    }

    private ConditionDTO parseSingleCondition(Expression expression) throws ExpressionParsingException {
        if (expression instanceof BinaryExpression binaryExpression) {
            return parseBinary(binaryExpression);
        }
        if (expression instanceof UnaryExpression unaryExpression) {
            return parseUnary(unaryExpression);
        }
        throw new ExpressionParsingException();
    }

    private ConditionDTO parseBinary(BinaryExpression expression) throws ExpressionParsingException {
        return new ConditionDTO(
                parseRefValue(expression.getLeft()),
                parseOpCode(expression.getOperator()),
                parseExprValue(expression.getRight())
        );
    }

    private ValueDTO parseRefValue(Expression expression) throws ExpressionParsingException {
        if (expression instanceof PropertyExpression fieldExpression) {
            Expression instance = fieldExpression.getInstance();
            StringBuilder buf = new StringBuilder();
            if (instance instanceof NodeExpression nodeExpression) {
                buf.append(nodeExpression.getNode().getName()).append(".");
            } else if (!(instance instanceof ThisExpression)) {
                throw new ExpressionParsingException();
            }
            buf.append(fieldExpression.getProperty().getName());
            return ValueDTO.refValue(buf.toString());
        } else if (expression instanceof NodeExpression nodeExpression) {
            return ValueDTO.refValue(nodeExpression.getNode().getName());
        }
        throw new ExpressionParsingException();
    }

    private ValueDTO parseExprValue(Expression expression) throws ExpressionParsingException {
        if (expression instanceof ConstantExpression constantExpression) {
            return ValueDTO.constValue(constantExpression.getValue().toFieldValueDTO());
        }
        if (expression instanceof PropertyExpression || expression instanceof NodeExpression) {
            return parseRefValue(expression);
        } else {
            return ValueDTO.exprValue(expression.buildSelf(VarType.NAME, false));
        }
    }

    private int parseOpCode(BinaryOperator operator) {
//        return ConditionOpCode.getByOperator(operator).code();
        return operator.code();
    }

    private ConditionDTO parseUnary(UnaryExpression expression) throws ExpressionParsingException {
        return new ConditionDTO(
                parseRefValue(expression.getOperand()),
                expression.getOperator().code(),
                null
        );
    }

}
