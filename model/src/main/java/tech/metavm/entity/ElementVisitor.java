package tech.metavm.entity;

import tech.metavm.expression.*;
import tech.metavm.flow.Function;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.Index;
import tech.metavm.object.view.*;

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
        throw new UnsupportedOperationException();
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

    public R visitField(Field field) {
        return visitElement(field);
    }

    public R visitFlow(Flow flow) {
        return visitElement(flow);
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

    public R visitNode(NodeRT node) {
        return visitElement(node);
    }

    public R visitScopeNode(ScopeNode node) {
        return visitNode(node);
    }

    public R visitGetElementNode(GetElementNode node) {
        return visitNode(node);
    }

    public R visitDeleteElementNode(RemoveElementNode node) {
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

    public R visitLoopNode(LoopNode node) {
        return visitScopeNode(node);
    }

    public R visitForeachNode(ForeachNode node) {
        return visitLoopNode(node);
    }

    public R visitWhileNode(WhileNode node) {
        return visitLoopNode(node);
    }

    public R visitCallNode(CallNode node) {
        return visitNode(node);
    }

    public R visitSubFlowNode(MethodCallNode node) {
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

    public R visitIndexSelectNode(IndexSelectNode node) {
        return visitNode(node);
    }

    public R visitIndexQueryNode(IndexScanNode node) {
        return visitNode(node);
    }

    public R visitIndexCountNode(IndexCountNode node) {
        return visitNode(node);
    }

    public R visitUpdateStaticNode(UpdateStaticNode node) {
        return visitNode(node);
    }

    public R visitValue(Value value) {
        return visitElement(value);
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

    public R visitFuncExpression(MethodExpression expression) {
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

    public R visitNothingType(NeverType neverType) {
        return visitType(neverType);
    }

    public R visitObjectType(AnyType anyType) {
        return visitType(anyType);
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

    public R visitExpressionPlaceholder(ExpressionPlaceholder expressionPlaceholder) {
        return visitExpression(expressionPlaceholder);
    }

    public R visitFlowFieldMapping(FlowFieldMapping flowFieldMapping) {
        return visitFieldMapping(flowFieldMapping);
    }

    public R visitDirectFieldMapping(DirectFieldMapping directFieldMapping) {
        return visitFieldMapping(directFieldMapping);
    }

    public R visitFieldMapping(FieldMapping fieldMapping) {
        return visitElement(fieldMapping);
    }

    public R visitMapping(Mapping mapping) {
        return visitElement(mapping);
    }

    public R visitObjectMapping(ObjectMapping mapping) {
        return visitMapping(mapping);
    }

    public R visitDefaultObjectMapping(FieldsObjectMapping mapping) {
        return visitObjectMapping(mapping);
    }

    public R visitComputedFieldMapping(ComputedFieldMapping computedFieldMapping) {
        return visitFieldMapping(computedFieldMapping);
    }

    public R visitCopyNode(CopyNode copyNode) {
        return visitNode(copyNode);
    }

    public R visitFunction(Function function) {
        return visitFlow(function);
    }

    public R visitMethod(Method method) {
        return visitFlow(method);
    }

    public R visitFunctionCallNode(FunctionCallNode functionCallNode) {
        return visitCallNode(functionCallNode);
    }

    public R visitCastNode(CastNode castNode) {
        return visitNode(castNode);
    }

    public R visitClearArrayNode(ClearArrayNode clearArrayNode) {
        return visitNode(clearArrayNode);
    }

    public R visitMapNode(MapNode mapNode) {
        return visitNode(mapNode);
    }

    public R visitUnmapNode(UnmapNode unmapNode) {
        return visitNode(unmapNode);
    }

}
