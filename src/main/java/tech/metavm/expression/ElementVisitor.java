package tech.metavm.expression;

import tech.metavm.flow.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;

public class ElementVisitor {

    public void visitType(Type type) {
        switch (type) {
            case ClassType classType -> visitClassType(classType);
            case ArrayType arrayType -> visitArrayType(arrayType);
            case UnionType unionType -> visitUnionType(unionType);
            case TypeVariable typeVariable -> visitTypeVariable(typeVariable);
            case PrimitiveType primitiveType -> visitPrimitiveType(primitiveType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public void visitClassType(ClassType classType) {
        classType.getFields().forEach(this::visitField);
        classType.getFlows().forEach(this::visitFlow);
        classType.getConstraints().forEach(this::visitConstraint);
    }

    public void visitConstraint(Constraint<?> constraint) {
        switch (constraint) {
            case Index index -> visitIndex(index);
            case CheckConstraint checkConstraint -> visitCheckConstraint(checkConstraint);
            default -> throw new IllegalStateException("Unexpected value: " + constraint);
        }
    }

    public void visitIndex(Index index) {
    }

    public void visitCheckConstraint(CheckConstraint checkConstraint) {
    }

    public void visitTypeVariable(TypeVariable typeVariable) {
    }

    public void visitArrayType(ArrayType arrayType) {
    }

    public void visitUnionType(UnionType unionType) {
    }

    public void visitPrimitiveType(PrimitiveType primitiveType) {
    }

    public void visitField(Field field) {
    }

    public void visitFlow(Flow flow) {
        visitScope(flow.getRootScope());
    }

    public void visitScope(ScopeRT scope) {
        scope.getNodes().forEach(this::visitNode);
    }

    public void visitBranch(Branch branch) {
        visitScope(branch.getScope());
    }

    public void visitNode(NodeRT<?> node) {
        switch (node) {
            case SelfNode selfNode-> visitSelfNode(selfNode);
            case InputNode inputNode-> visitInputNode(inputNode);
            case ReturnNode returnNode-> visitReturnNode(returnNode);
            case BranchNode branchNode-> visitBranchNode(branchNode);
            case LoopNode<?> loopNode-> visitLoopNode(loopNode);
            case UpdateObjectNode updateObjectNode-> visitUpdateObjectNode(updateObjectNode);
            case CallNode<?> callNode-> visitCallNode(callNode);
            case RaiseNode exceptionNode-> visitExceptionNode(exceptionNode);
            case ValueNode valueNode-> visitValueNode(valueNode);
            case UpdateStaticNode updateStaticNode-> visitUpdateStaticNode(updateStaticNode);
            case GetUniqueNode getUniqueNode-> visitGetUniqueNode(getUniqueNode);
            case DeleteObjectNode deleteObjectNode-> visitDeleteObjectNode(deleteObjectNode);
            case MergeNode mergeNode-> visitMergeNode(mergeNode);
            case NewArrayNode newArrayNode -> visitNewArrayNode(newArrayNode);
            case CheckNode exitBranchNode -> visitCheckNode(exitBranchNode);
            case TryNode tryNode -> visitTryNode(tryNode);
            case TryEndNode tryMergeNode -> vistTryMergeNode(tryMergeNode);
            case AddObjectNode addObjectNode -> visitAddObjectNode(addObjectNode);
            default -> throw new IllegalStateException("Unexpected node: " + node);
        }
    }

    public void visitAddObjectNode(AddObjectNode addObjectNode) {

    }

    public void vistTryMergeNode(TryEndNode tryMergeNode) {
    }

    public void visitTryNode(TryNode tryNode) {
    }


    public void visitCheckNode(CheckNode checkNode) {
    }

    public void visitNewArrayNode(NewArrayNode node) {
    }

    public void visitSelfNode(SelfNode selfNode) {
    }

    public void visitInputNode(InputNode inputNode) {
    }

    public void visitReturnNode(ReturnNode returnNode) {
    }

    public void visitBranchNode(BranchNode node) {
        node.getBranches().forEach(this::visitBranch);
    }

    public void visitLoopNode(LoopNode<?> node) {
        switch (node) {
            case WhileNode whileNode -> visitWhileNode(whileNode);
            case ForeachNode forEachNode -> visitForeachNode(forEachNode);
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    public void visitForeachNode(ForeachNode forEachNode) {
        visitScope(forEachNode.getBodyScope());
    }

    public void visitWhileNode(WhileNode node) {
        visitScope(node.getBodyScope());
    }

    public void visitCallNode(CallNode<?> callNode) {
        switch (callNode) {
            case NewNode newNode -> visitNewNode(newNode);
            case SubFlowNode subFlowNode -> visitSubFlowNode(subFlowNode);
            default -> throw new IllegalStateException("Unexpected value: " + callNode);
        }
    }

    public void visitSubFlowNode(SubFlowNode subFlowNode) {
    }

    public void visitNewNode(NewNode node) {
    }

    public void visitUpdateObjectNode(UpdateObjectNode node) {
    }

    public void visitExceptionNode(RaiseNode node) {
    }

    public void visitDeleteObjectNode(DeleteObjectNode node) {
    }

    public void visitMergeNode(MergeNode node) {
    }

    public void visitValueNode(ValueNode valueNode) {
    }

    public void visitGetUniqueNode(GetUniqueNode node) {
    }

    public void visitUpdateStaticNode(UpdateStaticNode node) {
    }

    private void visitValue(Value value) {
        visitExpression(value.getExpression());
    }

    public void visitExpression(Expression expression) {
        switch (expression) {
            case BinaryExpression binaryExpression-> visitBinaryExpression(binaryExpression);
            case UnaryExpression unaryExpression-> visitUnaryExpression(unaryExpression);
            case FieldExpression fieldExpression-> visitFieldExpression(fieldExpression);
            case ArrayAccessExpression arrayAccExpression-> visitArrayAccessExpression(arrayAccExpression);
            case FunctionExpression functionExpression-> visitFunctionExpression(functionExpression);
            case AsExpression asExpression-> visitAsExpression(asExpression);
            case ConditionalExpression conditionalExpression-> visitConditionalExpression(conditionalExpression);
            case ConstantExpression constantExpression-> visitConstantExpression(constantExpression);
            case VariableExpression variableExpression-> visitVariableExpression(variableExpression);
            case VariablePathExpression variablePathExpression ->
                    visitVariablePathExpression(variablePathExpression);
            case CursorExpression cursorExpression-> visitCursorExpression(cursorExpression);
            case AllMatchExpression allMatchExpression-> visitAllMatchExpression(allMatchExpression);
            case StaticFieldExpression staticFieldExpression-> visitStaticFieldExpression(staticFieldExpression);
            case NodeExpression nodeExpression-> visitNodeExpression(nodeExpression);
            case ThisExpression thisExpression-> visitThisExpression(thisExpression);
            case ArrayExpression arrayExpression-> visitArrayExpression(arrayExpression);
            case InstanceOfExpression instanceOfExpression-> visitInstanceOfExpression(instanceOfExpression);
            default -> throw new IllegalStateException("Unexpected expression: " + expression);
        };
    }

    public void visitBinaryExpression(BinaryExpression expression) {
    }

    public void visitUnaryExpression(UnaryExpression expression) {
    }

    public void visitFieldExpression(FieldExpression expression) {
    }

    public void visitArrayAccessExpression(ArrayAccessExpression expression) {
    }

    public void visitFunctionExpression(FunctionExpression expression) {
    }

    public void visitAsExpression(AsExpression expression) {
    }

    public void visitConditionalExpression(ConditionalExpression expression) {
    }

    public void visitConstantExpression(ConstantExpression expression) {
    }

    public void visitVariableExpression(VariableExpression expression) {
    }

    public void visitVariablePathExpression(VariablePathExpression expression) {
    }

    public void visitCursorExpression(CursorExpression expression) {
    }

    public void visitAllMatchExpression(AllMatchExpression expression) {
    }

    public void visitStaticFieldExpression(StaticFieldExpression expression) {
    }

    public void visitNodeExpression(NodeExpression expression) {
    }

    public void visitThisExpression(ThisExpression expression) {
    }

    public void visitArrayExpression(ArrayExpression expression) {
    }

    public void visitInstanceOfExpression(InstanceOfExpression expression) {
    }

}
