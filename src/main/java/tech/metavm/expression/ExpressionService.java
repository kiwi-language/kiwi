package tech.metavm.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.expression.dto.*;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.flow.ValueKind;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.PrimitiveFieldValueDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;
import tech.metavm.util.EntityContextBean;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ExpressionService extends EntityContextBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionService.class);

    public static final int CONTEXT_TYPE_TYPE = 1;

    public static final int CONTEXT_TYPE_FLOW = 2;

    public static final Set<Operator> SEARCH_EXPR_OPERATORS = Set.of(
        Operator.EQ, Operator.IN, Operator.STARTS_WITH, Operator.LIKE
    );

    public ExpressionService(InstanceContextFactory instanceContextFactory) {
        super(instanceContextFactory);
    }

    public BoolExprDTO parseBoolExpr(BoolExprParseRequest request) {
        try {
            IEntityContext context = newContext();
            Expression expression = ExpressionParser.parse(
                    extractExpr(request.value()),
                    getParsingContext(request.context(), context.getInstanceContext())
            );
            if(expression == null) {
                return new BoolExprDTO(List.of());
            }
            return new BoolExprDTO(parseConditionGroups(expression));
        }
        catch (ExpressionParsingException e) {
            LOGGER.warn("fail to parse expression: " + request.value(), e);
            throw BusinessException.invalidConditionExpr(NncUtils.toJSONString(request.value()));
        }
    }

    public List<InstanceSearchItemDTO> parseSearchText(long typeId, String searchText) {
        IInstanceContext context = newContext().getInstanceContext();
        ClassType type = context.getClassType(typeId);
        Expression expression = ExpressionParser.parse(searchText, new TypeParsingContext(type, context));
        return parseExpression(expression);
    }

    private List<InstanceSearchItemDTO> parseExpression(Expression expression) {
        List<InstanceSearchItemDTO> result = new ArrayList<>();
        parseExpression0(expression, result);
        return result;
    }

    private void parseExpression0(Expression expression, List<InstanceSearchItemDTO> result) {
        if(!(expression instanceof BinaryExpression binaryExpression)) {
            throw BusinessException.invalidExpression(expression.buildSelf(VarType.NAME));
        }
        if(binaryExpression.getOperator() == Operator.AND) {
            parseExpression0(binaryExpression.getFirst(), result);
            parseExpression0(binaryExpression.getSecond(), result);
        }
        else if(binaryExpression.getOperator() == Operator.OR) {
            parseExpression0(binaryExpression.getFirst(), result);
        }
        else {
            result.add(parseFieldExpr(binaryExpression));
        }
    }

    private InstanceSearchItemDTO parseFieldExpr(BinaryExpression binaryExpression) {
        Expression first = binaryExpression.getFirst();
        Expression second = binaryExpression.getSecond();
        Operator operator = binaryExpression.getOperator();
        if(!SEARCH_EXPR_OPERATORS.contains(operator)
                || !(first instanceof FieldExpression fieldExpr)
                || !(second instanceof ConstantExpression constExpr)) {
            throw BusinessException.invalidExpression(binaryExpression.buildSelf(VarType.NAME));
        }
        Object searchValue;
        if(constExpr.getValue() instanceof PrimitiveInstance primitiveInstance) {
            searchValue = primitiveInstance.getValue();
        }
        else {
            searchValue = NncUtils.requireNonNull(constExpr.getValue().getId());
        }
        return new InstanceSearchItemDTO(fieldExpr.getField().getId(), searchValue);
    }

    private String extractExpr(ValueDTO value) throws ExpressionParsingException {
        if(value.kind() == ValueKind.CONSTANT.code()) {
            if(value.value() instanceof PrimitiveFieldValueDTO primValue) {
                if(Boolean.TRUE.equals(primValue.getValue())) {
                    return "true";
                }
            }
            else {
                throw new ExpressionParsingException();
            }
        }
        if(value.kind() == ValueKind.REFERENCE.code()) {
            ExpressionFieldValueDTO exprValue = (ExpressionFieldValueDTO) value.value();
            return exprValue.getExpression().replaceAll("-", ".") + " = true";
        }
        else {
            return ((ExpressionFieldValueDTO) value.value()).getExpression();
        }
    }

    private ParsingContext getParsingContext(ParsingContextDTO contextDTO, IInstanceContext instanceContext) {
        IEntityContext context = newContext();
        if(contextDTO instanceof FlowParsingContextDTO flowContext) {
            NodeRT<?> prev = NncUtils.get(flowContext.getPrevNodeId(), context::getNode);
            List<NodeRT<?>> predecessors;
            if(prev == null) {
                ScopeRT scope = context.getScope(flowContext.getScopeId());
                predecessors = NncUtils.get(scope.getOwner(), NodeRT::getGlobalPredecessors);
            }
            else {
                predecessors = List.of(prev);
            }
            if(predecessors == null || predecessors.isEmpty()) {
                throw BusinessException.invalidParams("请求参数错误");
            }
            return new FlowParsingContext(predecessors, instanceContext);
        }
        else if(contextDTO instanceof TypeParsingContextDTO typeContext) {
            return new TypeParsingContext(
                    context.getClassType(typeContext.getTypeId()), instanceContext
            );
        }
        throw BusinessException.invalidParams("请求参数错误，未识别的解析上下文类型: " + contextDTO.getClass().getName());
    }

    private List<ConditionGroupDTO> parseConditionGroups(Expression expression) throws ExpressionParsingException {
        if(expression instanceof BinaryExpression binaryExpression) {
            if(binaryExpression.getOperator() == Operator.OR) {
                List<ConditionGroupDTO> firstGroups = parseConditionGroups(binaryExpression.getFirst());
                List<ConditionGroupDTO> secondGroups = parseConditionGroups(binaryExpression.getSecond());
                return NncUtils.merge(firstGroups, secondGroups);
            }
        }
        if(ExpressionUtil.isConstantTrue(expression)) {
            return List.of();
        }
        return List.of(new ConditionGroupDTO(parseConditions(expression)));
    }

    private List<ConditionDTO> parseConditions(Expression expression) throws ExpressionParsingException {
        if(expression instanceof BinaryExpression binaryExpression) {
            if(binaryExpression.getOperator() == Operator.AND) {
                List<ConditionDTO> firstGroups = parseConditions(binaryExpression.getFirst());
                List<ConditionDTO> secondGroups = parseConditions(binaryExpression.getSecond());
                return NncUtils.merge(firstGroups, secondGroups);
            }
        }
        if(ExpressionUtil.isConstantTrue(expression)) {
            return List.of();
        }
        return List.of(parseSingleCondition(expression));
    }

    private ConditionDTO parseSingleCondition(Expression expression) throws ExpressionParsingException {
        if(expression instanceof BinaryExpression binaryExpression) {
            return parseBinary(binaryExpression);
        }
        if(expression instanceof UnaryExpression unaryExpression) {
            return parseUnary(unaryExpression);
        }
        throw new ExpressionParsingException();
    }

    private ConditionDTO parseBinary(BinaryExpression expression) throws ExpressionParsingException {
        return new ConditionDTO(
                parseRefValue(expression.getFirst()),
                parseOpCode(expression.getOperator()),
                parseExprValue(expression.getSecond())
        );
    }

    private ValueDTO parseRefValue(Expression expression) throws ExpressionParsingException {
        if(expression instanceof FieldExpression fieldExpression) {
            Expression instance = fieldExpression.getInstance();
            StringBuilder buf = new StringBuilder();
            if(instance instanceof NodeExpression nodeExpression) {
                buf.append(nodeExpression.getNode().getName()).append(".");
            }
            else if(!(instance instanceof ThisExpression)) {
                throw new ExpressionParsingException();
            }
            buf.append(fieldExpression.getField().getName());
            return ValueDTO.refValue(buf.toString());
        }
        else if(expression instanceof NodeExpression nodeExpression) {
            return ValueDTO.refValue(nodeExpression.getNode().getName());
        }
        throw new ExpressionParsingException();
    }

    private ValueDTO parseExprValue(Expression expression) throws ExpressionParsingException {
        if(expression instanceof ConstantExpression constantExpression) {
            return ValueDTO.constValue(ExpressionUtil.expressionToConstant(constantExpression));
        }
        if(expression instanceof FieldExpression || expression instanceof NodeExpression) {
            return parseRefValue(expression);
        }
        else {
            return ValueDTO.exprValue(expression.buildSelf(VarType.NAME));
        }
    }

    private int parseOpCode(Operator operator) {
//        return ConditionOpCode.getByOperator(operator).code();
        return operator.code();
    }

    private ConditionDTO parseUnary(UnaryExpression expression) throws ExpressionParsingException {
        return new ConditionDTO(
                parseRefValue(expression.getOperand()),
                parseOpCode(expression.getOperator()),
                null
        );
    }

}
