package org.metavm.entity;

import org.metavm.expression.*;
import org.metavm.flow.*;
import org.metavm.object.type.EnumConstantDef;
import org.metavm.object.type.*;

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

    public R visitScope(Code code) {
        return visitElement(code);
    }

    public R visitNode(Node node) {
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

    public R visitExpressionPlaceholder(ExpressionPlaceholder expressionPlaceholder) {
        return visitExpression(expressionPlaceholder);
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

    public R visitLambdaRef(LambdaRef lambdaRef) {
        return visitElement(lambdaRef);
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

    public R visitIfNeNode(IfNeNode node) {
        return visitNode(node);
    }

    public R visitIfEqNode(IfEqNode node) {
        return visitNode(node);
    }

    public R visitNoopNode(NoopNode node) {
        return visitNode(node);
    }

    public R visitLongAddNode(LongAddNode node) {
        return visitNode(node);
    }

    public R visitLongSubNode(LongSubNode node) {
        return visitNode(node);
    }

    public R visitLongMulNode(LongMulNode node) {
        return visitNode(node);
    }

    public R visitLongDivNode(LongDivNode node) {
        return visitNode(node);
    }

    public R visitLongRemNode(LongRemNode node) {
        return visitNode(node);
    }

    public R visitLongShiftLeftNode(LongShiftLeftNode node) {
        return visitNode(node);
    }

    public R visitLongShiftRightNode(LongShiftRightNode node) {
        return visitNode(node);
    }

    public R visitLongUnsignedShiftRightNode(LongUnsignedShiftRightNode node) {
        return visitNode(node);
    }

    public R visitLongBitAndNode(LongBitAndNode node) {
        return visitNode(node);
    }

    public R visitLongBitOrNode(LongBitOrNode node) {
        return visitNode(node);
    }

    public R visitLongBitXorNode(LongBitXorNode node) {
        return visitNode(node);
    }

    public R visitLongNegNode(LongNegNode node) {
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

    public R visitLoadConstantNode(LoadConstantNode node) {
        return visitNode(node);
    }

    public R visitReturn(VoidReturnNode node) {
        return visitNode(node);
    }

    public R visitDupNode(DupNode node) {
        return visitNode(node);
    }

    public R visitNewArrayWithDimsNode(NewArrayWithDimsNode node) {
        return visitNode(node);
    }

    public R visitLoadTypeNode(LoadTypeNode node) {
        return visitNode(node);
    }

    public R visitPopNode(PopNode node) {
        return visitNode(node);
    }

    public R visitDupX2Node(DupX2Node node) {
        return visitNode(node);
    }

    public R visitDupX1Node(DupX1Node node) {
        return visitNode(node);
    }

    public R visitIndexRef(IndexRef indexRef) {
        return visitElement(indexRef);
    }

    public R visitIndexField(IndexField indexField) {
        return visitElement(indexField);
    }

    public R visitLoadParentNode(LoadParentNode node) {
        return visitNode(node);
    }

    public R visitNewChild(NewChildNode node) {
        return visitNewObjectNode(node);
    }

    public R visitConstantPool(ConstantPool constantPool) {
        return visitElement(constantPool);
    }

    public R visitLongToDoubleNode(LongToDoubleNode node) {
        return visitNode(node);
    }

    public R visitDoubleToLongNode(DoubleToLongNode node) {
        return visitNode(node);
    }

    public R visitDoubleAddNode(DoubleAddNode node) {
        return visitNode(node);
    }

    public R visitDoubleDivNode(DoubleDivNode node) {
        return visitNode(node);
    }

    public R visitDoubleSubNode(DoubleSubNode node) {
        return visitNode(node);
    }

    public R visitDoubleMulNode(DoubleMulNode node) {
        return visitNode(node);
    }

    public R visitDoubleRemNode(DoubleRemNode node) {
        return visitNode(node);
    }

    public R visitDoubleNegNode(DoubleNegNode node) {
        return visitNode(node);
    }

    public R visitIntAddNode(IntAddNode node) {
        return visitNode(node);
    }

    public R visitIntSubNode(IntSubNode node) {
        return visitNode(node);
    }

    public R visitIntMulNode(IntMulNode node) {
        return visitNode(node);
    }

    public R visitIntDivNode(IntDivNode node) {
        return visitNode(node);
    }

    public R visitIntRemNode(IntRemNode node) {
        return visitNode(node);
    }

    public R visitIntNegNode(IntNegNode node) {
        return visitNode(node);
    }

    public R visitIntToLongNode(IntToLongNode node) {
        return visitNode(node);
    }

    public R visitLongToIntNode(LongToIntNode node) {
        return visitNode(node);
    }

    public R visitIntToDoubleNode(IntToDoubleNode node) {
        return visitNode(node);
    }

    public R visitDoubleToIntNode(DoubleToIntNode node) {
        return visitNode(node);
    }

    public R visitIntShiftLeftNod(IntShiftLeftNode node) {
        return visitNode(node);
    }

    public R visitIntShiftRightNode(IntShiftRightNode node) {
        return visitNode(node);
    }

    public R visitIntUnsignedShiftRight(IntUnsignedShiftRightNode node) {
        return visitNode(node);
    }

    public R visitIntBitAndNode(IntBitAndNode node) {
        return visitNode(node);
    }

    public R visitIntBitOrNode(IntBitOrNode node) {
        return visitNode(node);
    }

    public R visitIntBitXorNode(IntBitXorNode node) {
        return visitNode(node);
    }

    public R visitLongCompareNode(LongCompareNode node) {
        return visitNode(node);
    }

    public R visitIntCompareNode(IntCompareNode node) {
        return visitNode(node);
    }

    public R visitDoubleCompareNode(DoubleCompareNode node) {
        return visitNode(node);
    }

    public R visitRefCompareEqNode(RefCompareEqNode node) {
        return visitNode(node);
    }

    public R visitRefCompareNeNode(RefCompareNeNode node) {
        return visitNode(node);
    }

    public R visitFloatAddNode(FloatAddNode node) {
        return visitNode(node);
    }

    public R visitFloatSubNode(FloatSubNode node) {
        return visitNode(node);
    }

    public R visitFloatMulNode(FloatMulNode node) {
        return visitNode(node);
    }

    public R visitFloatDivNode(FloatDivNode node) {
        return visitNode(node);
    }

    public R visitFloatRemNode(FloatRemNode node) {
        return visitNode(node);
    }

    public R visitFloatNegNode(FloatNegNode node) {
        return visitNode(node);
    }

    public R visitFloatCompareNode(FloatCompareNode node) {
        return visitNode(node);
    }

    public R visitFloatToDoubleNode(FloattoDoubleNode node) {
        return visitNode(node);
    }

    public R visitFloatToLongNode(FloatToLongNode node) {
        return visitNode(node);
    }

    public R visitFloatToIntNode(FloatToIntNode node) {
        return visitNode(node);
    }

    public R visitLongToFloatNode(LongToFloatNode node) {
        return visitNode(node);
    }

    public R visitIntToFloatNode(IntToFloatNode node) {
        return visitNode(node);
    }

    public R visitDoubleToFloatNode(DoubleToFloat node) {
        return visitNode(node);
    }

    public R visitIntToShortNode(IntToShortNode node) {
        return visitNode(node);
    }

    public R visitIntToByteNode(IntToByteNode node) {
        return visitNode(node);
    }

    public R visitIntToCharNode(IntToCharNode node) {
        return visitNode(node);
    }
}
