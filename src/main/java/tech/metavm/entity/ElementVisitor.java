package tech.metavm.entity;

import org.apache.commons.lang3.NotImplementedException;
import tech.metavm.expression.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;

public abstract class ElementVisitor<R> {

    public R visitType(Type type) {
        return visitElement(type);
    }

    public R visitClassType(ClassType type) {
        return visitType(type);
    }

    public R visitConstraint(Constraint constraint) {
        return visitElement(constraint);
    }

    public R visitElement(Element element) {
        throw new NotImplementedException();
    }

    public R visitIndex(Index index) {
        return visitConstraint(index);
    }

    public R visitCheckConstraint(CheckConstraint checkConstraint) {
        return visitConstraint(checkConstraint);
    }

    public R visitTypeVariable(TypeVariable type) {
        return visitType(type);
    }

    public R visitArrayType(ArrayType type) {
        return visitType(type);
    }

    public R visitUnionType(UnionType type) {
        return visitType(type);
    }

    public R visitIntersectionType(IntersectionType type) {
        return visitType(type);
    }

    public R visitPrimitiveType(PrimitiveType type) {
        return visitType(type);
    }

    public R visitProperty(Property property) {
        return visitElement(property);
    }

    public R visitField(Field field) {
        return visitProperty(field);
    }

    public R visitFlow(Flow flow) {
        return visitProperty(flow);
    }

    public R visitParameter(Parameter parameter) {
        return visitElement(parameter);
    }

    public R visitScope(ScopeRT scope) {
        return visitElement(scope);
    }

    public R visitBranch(Branch branch) {
        return visitElement(branch);
    }

    public R visitNode(NodeRT<?> node) {
        return visitElement(node);
    }

    public R visitScopeNode(ScopeNode<?> node) {
        return visitNode(node);
    }

    public R visitGetElementNode(GetElementNode node) {
        return visitNode(node);
    }

    public R visitDeleteElementNode(DeleteElementNode node) {
        return visitNode(node);
    }

    public R visitAddElementNode(AddElementNode node) {
        return visitNode(node);
    }

    public R visitFunctionNode(FunctionNode node) {
        return visitNode(node);
    }

    public R visitLambdaNode(LambdaNode node) {
        return visitNode(node);
    }

    public R visitAddObjectNode(AddObjectNode node) {
        return visitScopeNode(node);
    }

    public R visitTryEndNode(TryEndNode node) {
        return visitNode(node);
    }

    public R visitTryNode(TryNode node) {
        return visitScopeNode(node);
    }

    public R visitCheckNode(CheckNode node) {
        return visitNode(node);
    }

    public R visitNewArrayNode(NewArrayNode node) {
        return visitNode(node);
    }

    public R visitSelfNode(SelfNode node) {
        return visitNode(node);
    }

    public R visitInputNode(InputNode node) {
        return visitNode(node);
    }

    public R visitReturnNode(ReturnNode node) {
        return visitNode(node);
    }

    public R visitBranchNode(BranchNode node) {
        return visitNode(node);
    }

    public R visitLoopNode(LoopNode<?> node) {
        return visitScopeNode(node);
    }

    public R visitForeachNode(ForeachNode node) {
        return visitLoopNode(node);
    }

    public R visitWhileNode(WhileNode node) {
        return visitLoopNode(node);
    }

    public R visitCallNode(CallNode<?> node) {
        return visitNode(node);
    }

    public R visitSubFlowNode(SubFlowNode node) {
        return visitCallNode(node);
    }

    public R visitNewObjectNode(NewObjectNode node) {
        return visitCallNode(node);
    }

    public R visitUpdateObjectNode(UpdateObjectNode node) {
        return visitNode(node);
    }

    public R visitRaiseNode(RaiseNode node) {
        return visitNode(node);
    }

    public R visitDeleteObjectNode(DeleteObjectNode node) {
        return visitNode(node);
    }

    public R visitMergeNode(MergeNode node) {
        return visitNode(node);
    }

    public R visitValueNode(ValueNode node) {
        return visitNode(node);
    }

    public R visitGetUniqueNode(GetUniqueNode node) {
        return visitNode(node);
    }

    public R visitUpdateStaticNode(UpdateStaticNode node) {
        return visitNode(node);
    }

    public R visitValue(Value value) {
        return visitElement(value);
    }

    public R visitConstantValue(ConstantValue value) {
        return visitValue(value);
    }

    public R visitExpressionValue(ExpressionValue value) {
        return visitValue(value);
    }

    public R visitReferenceValue(ReferenceValue value) {
        return visitValue(value);
    }

    public R visitExpression(Expression expression) {
        return visitElement(expression);
    }

    public R visitBinaryExpression(BinaryExpression expression) {
        return visitExpression(expression);
    }

    public R visitUnaryExpression(UnaryExpression expression) {
        return visitExpression(expression);
    }

    public R visitPropertyExpression(PropertyExpression expression) {
        return visitExpression(expression);
    }

    public R visitArrayAccessExpression(ArrayAccessExpression expression) {
        return visitExpression(expression);
    }

    public R visitFunctionExpression(FunctionExpression expression) {
        return visitExpression(expression);
    }

    public R visitFuncExpression(FuncExpression expression) {
        return visitExpression(expression);
    }

    public R visitAsExpression(AsExpression expression) {
        return visitExpression(expression);
    }

    public R visitConditionalExpression(ConditionalExpression expression) {
        return visitExpression(expression);
    }

    public R visitConstantExpression(ConstantExpression expression) {
        return visitExpression(expression);
    }

    public R visitVariableExpression(VariableExpression expression) {
        return visitExpression(expression);
    }

    public R visitVariablePathExpression(VariablePathExpression expression) {
        return visitExpression(expression);
    }

    public R visitCursorExpression(CursorExpression expression) {
        return visitExpression(expression);
    }

    public R visitAllMatchExpression(AllMatchExpression expression) {
        return visitExpression(expression);
    }

    public R visitStaticFieldExpression(StaticFieldExpression expression) {
        return visitExpression(expression);
    }

    public R visitNodeExpression(NodeExpression expression) {
        return visitExpression(expression);
    }

    public R visitThisExpression(ThisExpression expression) {
        return visitExpression(expression);
    }

    public R visitArrayExpression(ArrayExpression expression) {
        return visitExpression(expression);
    }

    public R visitInstanceOfExpression(InstanceOfExpression expression) {
        return visitExpression(expression);
    }

    public R visitFunctionType(FunctionType functionType) {
        return visitType(functionType);
    }

    public R visitNothingType(NothingType nothingType) {
        return visitType(nothingType);
    }

    public R visitObjectType(ObjectType objectType) {
        return visitType(objectType);
    }

    public R visitUncertainType(UncertainType uncertainType) {
        return visitType(uncertainType);
    }

    public R visitArgument(Argument argument) {
        return visitElement(argument);
    }

    public R visitParentRef(ParentRef parentRef) {
        return visitElement(parentRef);
    }
}
