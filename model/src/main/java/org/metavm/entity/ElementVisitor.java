package org.metavm.entity;

import org.metavm.expression.*;
import org.metavm.flow.*;
import org.metavm.object.type.EnumConstantDef;
import org.metavm.object.type.*;
import org.metavm.object.view.*;

public abstract class ElementVisitor<R> {

    public R visitType(Type type) {
        return visitElement(type);
    }

    public R visitKlass(Klass klass) {
        return visitElement(klass);
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

    public R visitClassType(ClassType type) {
        return visitType(type);
    }

    public R visitTypeVariable(TypeVariable typeVariable) {
        return visitElement(typeVariable);
    }

    public R visitCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable) {
        return visitElement(capturedTypeVariable);
    }

    public R visitVariableType(VariableType type) {
        return visitType(type);
    }

    public R visitCapturedType(CapturedType type) {
        return visitType(type);
    }

    public R visitCompositeType(CompositeType type) {
        return visitType(type);
    }

    public R visitArrayType(ArrayType type) {
        return visitCompositeType(type);
    }

    public R visitUnionType(UnionType type) {
        return visitCompositeType(type);
    }

    public R visitIntersectionType(IntersectionType type) {
        return visitCompositeType(type);
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

    public R visitNode(NodeRT node) {
        return visitElement(node);
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

    public R visitSetElementNode(SetElementNode node) {
        return visitNode(node);
    }

    public R visitFunctionNode(FunctionNode node) {
        return visitNode(node);
    }

    public R visitLambdaEnterNode(LambdaNode node) {
        return visitNode(node);
    }

    public R visitAddObjectNode(AddObjectNode node) {
        return visitNode(node);
    }

    public R visitTryExitNode(TryExitNode node) {
        return visitNode(node);
    }

    public R visitTryEnterNode(TryEnterNode node) {
        return visitNode(node);
    }

    public R visitNewArrayNode(NewArrayNode node) {
        return visitNode(node);
    }

    public R visitReturnNode(ReturnNode node) {
        return visitNode(node);
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

    public R visitSetFieldNode(SetFieldNode node) {
        return visitNode(node);
    }

    public R visitRaiseNode(RaiseNode node) {
        return visitNode(node);
    }

    public R visitDeleteObjectNode(DeleteObjectNode node) {
        return visitNode(node);
    }

    public R visitGetUniqueNode(GetUniqueNode node) {
        return visitNode(node);
    }

    public R visitIndexSelectNode(IndexSelectNode node) {
        return visitNode(node);
    }

    public R visitIndexSelectFirstNode(IndexSelectFirstNode node) {
        return visitNode(node);
    }

    public R visitIndexQueryNode(IndexScanNode node) {
        return visitNode(node);
    }

    public R visitIndexCountNode(IndexCountNode node) {
        return visitNode(node);
    }

    public R visitSetStaticNode(SetStaticNode node) {
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

    public R visitStaticFieldExpression(StaticPropertyExpression expression) {
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

    public R visitFunctionType(FunctionType type) {
        return visitCompositeType(type);
    }

    public R visitNeverType(NeverType type) {
        return visitType(type);
    }

    public R visitAnyType(AnyType type) {
        return visitType(type);
    }

    public R visitUncertainType(UncertainType type) {
        return visitCompositeType(type);
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

    public R visitGotoNode(GotoNode gotoNode) {
        return visitNode(gotoNode);
    }

    public R visitFieldRef(FieldRef fieldRef) {
        return visitElement(fieldRef);
    }

    public R visitFlowRef(FlowRef flowRef) {
        return visitElement(flowRef);
    }

    public R visitMethodRef(MethodRef methodRef) {
        return visitFlowRef(methodRef);
    }

    public R visitFunctionRef(FunctionRef functionRef) {
        return visitFlowRef(functionRef);
    }

    public R visitParameterRef(ParameterRef parameterRef) {
        return visitElement(parameterRef);
    }

    public R visitLambdaNodeRef(LambdaRef lambdaRef) {
        return visitElement(lambdaRef);
    }

    public R visitObjectMappingRef(ObjectMappingRef objectMappingRef) {
        return visitElement(objectMappingRef);
    }

    public R visitEnumConstantDef(EnumConstantDef enumConstantDef) {
        return visitElement(enumConstantDef);
    }

    public R visitTargetNode(TargetNode targetNode) {
        return visitNode(targetNode);
    }

    public R visitTypeLiteralExpression(TypeLiteralExpression typeLiteralExpression) {
        return visitExpression(typeLiteralExpression);
    }

    public R visitNonNullNode(NonNullNode nonNullNode) {
        return visitNode(nonNullNode);
    }

    public R visitNeverExpression(NeverExpression neverExpression) {
        return visitExpression(neverExpression);
    }

    public R visitIfNode(IfNode node) {
        return visitNode(node);
    }

    public R visitIfNotNode(IfNotNode node) {
        return visitNode(node);
    }

    public R visitNoopNode(NoopNode node) {
        return visitNode(node);
    }

    public R visitAddNode(AddNode node) {
        return visitNode(node);
    }

    public R visitAndNode(AndNode node) {
        return visitNode(node);
    }

    public R visitOrNode(OrNode node) {
        return visitNode(node);
    }

    public R visitSubNode(SubNode node) {
        return visitNode(node);
    }

    public R visitMultiplyNode(MultiplyNode node) {
        return visitNode(node);
    }

    public R visitDivideNode(DivideNode node) {
        return visitNode(node);
    }

    public R visitRemainderNode(RemainderNode node) {
        return visitNode(node);
    }

    public R visitLeftShiftNode(LeftShiftNode node) {
        return visitNode(node);
    }

    public R visitRightShiftNode(RightShiftNode node) {
        return visitNode(node);
    }

    public R visitUnsignedRightShift(UnsignedRightShiftNode node) {
        return visitNode(node);
    }

    public R visitBitwiseAndNode(BitwiseAndNode node) {
        return visitNode(node);
    }

    public R visitBitwiseOrNode(BitwiseOrNode node) {
        return visitNode(node);
    }

    public R visitBitwiseXorNode(BitwiseXorNode node) {
        return visitNode(node);
    }

    public R visitNegateNode(NegateNode node) {
        return visitNode(node);
    }

    public R visitBitwiseComplementNode(BitwiseComplementNode node) {
        return visitNode(node);
    }

    public R visitEqNode(EqNode node) {
        return visitNode(node);
    }

    public R visitNeNode(NeNode node) {
        return visitNode(node);
    }

    public R visitGtNode(GtNode node) {
        return visitNode(node);
    }

    public R visitGeNode(GeNode node) {
        return visitNode(node);
    }

    public R visitLtNode(LtNode node) {
        return visitNode(node);
    }

    public R visitLeNode(LeNode node) {
        return visitNode(node);
    }

    public R visitInstanceOfNode(InstanceOfNode node) {
        return visitNode(node);
    }

    public R visitGetFieldNode(GetPropertyNode node) {
        return visitNode(node);
    }

    public R visitGetStaticNode(GetStaticNode node) {
        return visitNode(node);
    }

    public R visitNodeNode(NotNode node) {
        return visitNode(node);
    }

    public R visitArrayLengthNode(ArrayLengthNode node) {
        return visitNode(node);
    }

    public R visitExpressionValue(ExpressionValue value) {
        return visitValue(value);
    }

    public R visitPropertyValue(PropertyValue value) {
        return visitValue(value);
    }

    public R visitNeverValue(NeverValue value) {
        return visitValue(value);
    }

    public R visitArrayValue(ArrayValue value) {
        return visitValue(value);
    }

    public R visitConstantValue(ConstantValue value) {
        return visitValue(value);
    }

    public R visitTypeValue(TypeValue value) {
        return visitValue(value);
    }

    public R visitNodeValue(NodeValue value) {
        return visitValue(value);
    }

    public R visitStoreNode(StoreNode node) {
        return visitVariableAccessNode(node);
    }

    public R visitLoadNode(LoadNode node) {
        return visitVariableAccessNode(node);
    }

    public R visitLoadContextSlotNode(LoadContextSlotNode node) {
        return visitNode(node);
    }

    public R visitStoreContextSlotNode(StoreContextSlotNode node) {
        return visitNode(node);
    }

    public R visitLambda(Lambda lambda) {
        return visitElement(lambda);
    }

    public R visitVariableAccessNode(VariableAccessNode node) {
        return visitNode(node);
    }

}
