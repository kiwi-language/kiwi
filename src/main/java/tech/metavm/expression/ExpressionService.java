package tech.metavm.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.expression.dto.*;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.flow.ValueKind;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.*;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;
import tech.metavm.util.EntityContextBean;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class ExpressionService extends EntityContextBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExpressionService.class);

    public static final int CONTEXT_TYPE_TYPE = 1;

    public static final int CONTEXT_TYPE_FLOW = 2;

    public ExpressionService(InstanceContextFactory instanceContextFactory) {
        super(instanceContextFactory);
    }

    public BoolExprDTO parseBoolExpr(BoolExprParseRequest request) {
        try {
            Expression expression = ExpressionParser.parse(extractExpr(request.value()), getParsingContext(request.context()));
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

    private String extractExpr(ValueDTO value) throws ExpressionParsingException {
        if(value.kind() == ValueKind.CONSTANT.code()) {
            if(Boolean.TRUE.equals(value.value())) {
                return "true";
            }
            else {
                throw new ExpressionParsingException();
            }
        }
        if(value.kind() == ValueKind.REFERENCE.code()) {
            String ref = (String) value.value();
            return ref.replaceAll("-", ".") + " = true";
        }
        else {
            return (String) value.value();
        }
    }

    private ParsingContext getParsingContext(ParsingContextDTO contextDTO) {
        IEntityContext context = newContext();
        if(contextDTO instanceof FlowParsingContextDTO flowContext) {
            NodeRT<?> prev = NncUtils.get(flowContext.getPrevNodeId(), context::getNode);
            if(prev == null) {
                ScopeRT scope = context.getScope(flowContext.getScopeId());
                prev = NncUtils.get(scope.getOwner(), NodeRT::getGlobalPredecessor);
            }
            if(prev == null) {
                throw BusinessException.invalidParams("请求参数错误");
            }
            return new FlowParsingContext(/*context.getInstanceContext(), */prev);
        }
        else if(contextDTO instanceof TypeParsingContextDTO typeContext) {
            return new TypeParsingContext(
                    context.getType(typeContext.getTypeId())
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
            buf.append(NncUtils.join(fieldExpression.getFieldPath(), Field::getName, "."));
            return ValueDTO.refValue(buf.toString());
        }
        else if(expression instanceof NodeExpression nodeExpression) {
            return ValueDTO.refValue(nodeExpression.getNode().getName());
        }
        throw new ExpressionParsingException();
    }

    private ValueDTO parseExprValue(Expression expression) throws ExpressionParsingException {
        if(expression instanceof ConstantExpression constantExpression) {
            return ValueDTO.constValue(constantExpression.getValue());
        }
        if(expression instanceof FieldExpression || expression instanceof NodeExpression) {
            return parseRefValue(expression);
        }
        else {
            return ValueDTO.exprValue(expression.buildSelf(VarType.NAME));
        }
    }

    private int parseOpCode(Operator operator) {
        return ConditionOpCode.getByOperator(operator).code();
    }

    private ConditionDTO parseUnary(UnaryExpression expression) throws ExpressionParsingException {
        return new ConditionDTO(
                parseRefValue(expression.getOperand()),
                parseOpCode(expression.getOperator()),
                null
        );
    }

}
