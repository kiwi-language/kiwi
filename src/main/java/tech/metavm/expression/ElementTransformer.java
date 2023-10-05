package tech.metavm.expression;

import tech.metavm.flow.FlowBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.object.meta.TypeUtil.getParameterizedCode;
import static tech.metavm.object.meta.TypeUtil.getParameterizedName;

public class ElementTransformer {

    private final Map<NodeRT<?>, NodeRT<?>> nodeMap = new HashMap<>();
    private final LinkedList<ClassType> classes = new LinkedList<>();
    private final LinkedList<Flow> flows = new LinkedList<>();
    private final LinkedList<ScopeRT> scopes = new LinkedList<>();
    protected final TypeFactory typeFactory;

    public ElementTransformer() {
        this(new DefaultTypeFactory(ModelDefRegistry::getType));
    }

    public ElementTransformer(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public Type transformType(Type type) {
        return switch (type) {
            case ClassType classType -> transformClassType(classType);
            case ArrayType arrayType -> transformArrayType(arrayType);
            case UnionType unionType -> transformUnionType(unionType);
            case TypeVariable typeVariable -> transformTypeVariable(typeVariable);
            case PrimitiveType primitiveType -> transformPrimitiveType(primitiveType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public ClassType transformClassType(ClassType classType) {
        return transformClassType(classType, classType.getName(), classType.getCode(),
                classType.getTemplate(), classType.getTypeArguments());
    }

    public ClassType transformClassType(ClassType classType,
                                        String name,
                                        @Nullable String code,
                                        @Nullable ClassType template,
                                        List<Type> typeArgs) {
        var transformed = ClassBuilder.newBuilder(name, code)
                .desc(classType.getDesc())
                .source(classType.getSource())
                .category(classType.getCategory())
                .anonymous(classType.isAnonymous())
                .template(template)
                .dependencies(NncUtils.map(classType.getDependencies(),
                        dep -> (ClassType) transformType(dep)))
                .typeArguments(typeArgs)
                .interfaces(NncUtils.map(
                        classType.getInterfaces(), it -> (ClassType) transformType(it)
                ))
                .superType(classType.getSuperType() != null ?
                        (ClassType) transformType(classType.getSuperType()) : null)
                .ephemeral(classType.isEphemeral())
                .build();
        onClassTypeTransformed(transformed);
        enterClass(transformed);
        transformClassBody(classType);
        exitClass();
        return transformed;
    }

    protected void transformClassBody(ClassType classType) {
        classType.getDeclaredFields().forEach(this::transformField);
        classType.getDeclaredFlows().forEach(this::transformFlow);
    }

    protected void onClassTypeTransformed(ClassType transformedClassType) {

    }

    public Type transformTypeVariable(TypeVariable typeVariable) {
        return typeVariable;
    }

    public Type transformArrayType(ArrayType arrayType) {
        return new ArrayType(
                null,
                transformType(arrayType.getElementType()),
                arrayType.isEphemeral()
        );
    }

    public Type transformUnionType(UnionType unionType) {
        return new UnionType(
                null, NncUtils.mapUnique(unionType.getMembers(), this::transformType)
        );
    }

    public Type transformPrimitiveType(PrimitiveType primitiveType) {
        return primitiveType;
    }

    public Field transformField(Field field) {
        var transformed = FieldBuilder.newBuilder(
                        field.getName(), field.getCode(), currentClass(), transformType(field.getType()))
                .asTitle(field.isAsTitle())
                .nullType(typeFactory.getNullType())
                .isChild(field.isChildField())
                .access(field.getAccess())
                .unique(field.isUnique())
                .isStatic(field.isStatic())
                .template(field.getTemplate())
                .build();
        return transformed;
    }

    public Flow transformFlow(Flow flow) {
        var typeArguments = NncUtils.map(flow.getTypeArguments(), this::transformType);
        return transformFlow(flow, flow.getTemplate(), typeArguments);
    }

    public Flow transformFlow(Flow flow, Flow template, List<Type> typeArguments) {
        String name = template == null ? flow.getName() : getParameterizedName(flow.getName(), typeArguments);
        String code = template == null ? flow.getCode() : getParameterizedCode(flow.getCode(), typeArguments);
        Flow concreteOverriden;
        if (flow.getOverridden() != null) {
            var overridenType = flow.getOverridden().getDeclaringType();
            var concreteOverridenType = (ClassType) transformType(getSuperType(flow.getDeclaringType(), overridenType));
            concreteOverriden = concreteOverridenType.getFlows().get(
                    overridenType.getFlows().indexOf(flow.getOverridden())
            );
        } else {
            concreteOverriden = null;
        }
        var declaringType = hasCurrentClass() ? currentClass() : flow.getDeclaringType();
        var transformed = FlowBuilder.newBuilder(declaringType, name, code)
                .template(template)
                .nullType(typeFactory.getNullType())
                .typeArguments(typeArguments)
                .overriden(concreteOverriden)
                .isAbstract(flow.isAbstract())
                .isNative(flow.isNative())
                .isConstructor(flow.isConstructor())
                .parameters(
                        concreteOverriden == null ?
                                NncUtils.map(flow.getParameters(), this::transformParameter) : null
                )
                .outputType(transformType(flow.getReturnType()))
                .build();

        enterFlow(transformed);
        enterScope(transformed.getRootScope());
        transformed.setRootScope(transformScope(flow.getRootScope()));
        exitScope();
        exitFlow();
        return transformed;
    }

    public Parameter transformParameter(Parameter parameter) {
        return new Parameter(
                null,
                parameter.getName(),
                parameter.getCode(),
                transformType(parameter.getType())
        );
    }

    public ScopeRT transformScope(ScopeRT scope) {
        var transformed = new ScopeRT(flow(),
                NncUtils.get(scope.getOwner(), this::getTransformedNode), scope.isWithBackEdge());
        enterScope(transformed);
        scope.getNodes().forEach(this::transformNode);
        exitScope();
        return transformed;
    }

    private ClassType getSuperType(ClassType currentType, ClassType targetType) {
        return currentType.findSuperTypeRequired(superType -> superType.templateEquals(targetType));
    }

    private ClassType getRawType(Type type) {
        if (type instanceof ClassType classType) {
            return classType;
        } else {
            throw new InternalException("Can not get raw type for type " + type);
        }
    }

    private LinkedList<ClassType> getInheritanceChain(ClassType currentType, ClassType superTypeTmp) {
        LinkedList<ClassType> inheritanceChain = new LinkedList<>();
        getInheritanceChain(currentType, superTypeTmp, inheritanceChain);
        return inheritanceChain;
    }

    private boolean getInheritanceChain(Type currentType, ClassType superType, LinkedList<ClassType> chain) {
        if (getRawType(currentType) == superType) {
            return true;
        }
        for (ClassType s : getRawType(currentType).getSupers()) {
            chain.add(getRawType(currentType));
            if (getInheritanceChain(s, superType, chain)) {
                return true;
            }
            chain.removeLast();
        }
        return false;
    }

    protected ClassType currentClass() {
        return NncUtils.requireNonNull(classes.peek());
    }

    protected boolean hasCurrentClass() {
        return !classes.isEmpty();
    }

    public void enterClass(ClassType classType) {
        classes.push(classType);
    }

    public void exitClass() {
        classes.pop();
    }

    private NodeRT<?> transformNode(NodeRT<?> node) {
        var transformed = switch (node) {
            case SelfNode selfNode -> transformSelfNode(selfNode);
            case InputNode inputNode -> transformInputNode(inputNode);
            case ReturnNode returnNode -> transformReturnNode(returnNode);
            case BranchNode branchNode -> transformBranchNode(branchNode);
            case WhileNode whileNode -> transformWhileNode(whileNode);
            case UpdateObjectNode updateObjectNode -> transformUpdateObjectNode(updateObjectNode);
            case NewNode newNode -> transformNewNode(newNode);
            case SubFlowNode subFlowNode -> transformSubFlowNode(subFlowNode);
            case RaiseNode exceptionNode -> transformExceptionNode(exceptionNode);
            case ValueNode valueNode -> transformValueNode(valueNode);
            case UpdateStaticNode updateStaticNode -> transformUpdateStaticNode(updateStaticNode);
            case GetUniqueNode getUniqueNode -> transformGetUniqueNode(getUniqueNode);
            case DeleteObjectNode deleteObjectNode -> transformDeleteObjectNode(deleteObjectNode);
            case MergeNode mergeNode -> transformMergeNode(mergeNode);
            case AddObjectNode addObjectNode -> transformAddObjectNode(addObjectNode);
            case CheckNode checkNode -> transformCheckNode(checkNode);
            default -> throw new IllegalStateException("Unexpected node: " + node);
        };
        nodeMap.put(node, transformed);
        return transformed;
    }

    private CheckNode transformCheckNode(CheckNode checkNode) {
        return new CheckNode(
                null, checkNode.getName(), getTransformedNode(checkNode.getPredecessor()),
                scope(), transformValue(checkNode.getCondition())
        );
    }

    private AddObjectNode transformAddObjectNode(AddObjectNode addObjectNode) {
        return new AddObjectNode(null, addObjectNode.getName(),
                (ClassType) transformType(addObjectNode.getType()),
                getTransformedNode(addObjectNode.getPredecessor()),
                scope()
        );
    }

    private SelfNode transformSelfNode(SelfNode selfNode) {
        return new SelfNode(null, selfNode.getName(),
                getTransformedNode(selfNode.getPredecessor()),
                scope());
    }

    private InputNode transformInputNode(InputNode inputNode) {
        var inputType = ClassBuilder.newBuilder("输入类型", "InputType")
                .temporary().build();
        for (Parameter parameter : flow().getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType, parameter.getType())
                    .build();
        }
        return new InputNode(
                null,
                inputNode.getName(),
                inputType,
                getTransformedNode(inputNode.getPredecessor()),
                scope()
        );
    }

    private ReturnNode transformReturnNode(ReturnNode returnNode) {
        var transformed = new ReturnNode(
                null,
                returnNode.getName(),
                getTransformedNode(returnNode.getPredecessor()),
                scope()
        );
        if (returnNode.getValue() != null) {
            transformed.setValue(transformValue(returnNode.getValue()));
        }
        return transformed;
    }

    private BranchNode transformBranchNode(BranchNode branchNode) {
        var transformed = new BranchNode(
                null,
                branchNode.getName(),
                branchNode.isInclusive(),
                getTransformedNode(branchNode.getPredecessor()),
                scope()
        );
        for (Branch branch : branchNode.getBranches()) {
            var branchCopy = new Branch(
                    branch.getIndex(),
                    transformValue(branch.getCondition()),
                    branch.isPreselected(),
                    new ScopeRT(flow(), transformed),
                    transformed
            );
            enterScope(branchCopy.getScope());
            branch.getScope().getNodes().forEach(this::transformNode);
            exitScope();
        }
        return transformed;
    }

    private WhileNode transformWhileNode(WhileNode node) {
        var transformed = new WhileNode(
                null,
                node.getName(),
                transformType(node.getType()),
                getTransformedNode(node.getPredecessor()),
                scope(),
                new ExpressionValue(ExpressionUtil.trueExpression())
        );
        nodeMap.put(node, transformed);
        enterScope(transformed.getBodyScope());
        node.getBodyScope().getNodes().forEach(this::transformNode);
        exitScope();
        transformed.setCondition(transformValue(node.getCondition()));
        for (LoopField field : node.getFields()) {
            transformed.setField(
                    transformed.getType().getFieldByName(field.getField().getName()),
                    transformValue(field.getInitialValue()),
                    transformValue(field.getUpdatedValue())
            );
        }
        return transformed;
    }

    private SubFlowNode transformSubFlowNode(SubFlowNode subFlowNode) {
        var concreteSelf = transformValue(subFlowNode.getSelfId());
        var selfType = (ClassType) concreteSelf.getType();
        var subFlow = selfType.getFlows().get(
                subFlowNode.getSubFlow().getDeclaringType().getFlows().indexOf(subFlowNode.getSubFlow())
        );
        subFlow = selfType.getFlows().get(
                subFlow.getDeclaringType().getFlows().indexOf(subFlow)
        );
        var transformedSubFlow = transformFlowReference(subFlow);
        List<Argument> arguments = new ArrayList<>();
        NncUtils.biForEach(
                subFlowNode.getArguments(),
                subFlow.getParameters(),
                (arg, param) -> arguments.add(new Argument(null, param, transformValue(arg.getValue())))
        );
        return new SubFlowNode(
                null,
                subFlowNode.getName(),
                getTransformedNode(subFlowNode.getPredecessor()),
                scope(),
                concreteSelf,
                arguments,
                transformedSubFlow
        );
    }

    private NewNode transformNewNode(NewNode node) {
        var concreteType = (ClassType) transformType(node.getType());
        var subFlow = concreteType.getFlows().get(
                node.getSubFlow().getDeclaringType().getFlows().indexOf(node.getSubFlow())
        );
        var transformedSubFlow = transformFlowReference(subFlow);
        List<Argument> arguments = new ArrayList<>();
        NncUtils.biForEach(
                node.getArguments(),
                flow().getParameters(),
                (arg, param) -> arguments.add(new Argument(null, param, transformValue(arg.getValue())))
        );
        return new NewNode(
                null,
                node.getName(),
                transformedSubFlow,
                arguments,
                getTransformedNode(node.getPredecessor()),
                scope()
        );
    }

    private UpdateObjectNode transformUpdateObjectNode(UpdateObjectNode node) {
        var self = transformValue(node.getObjectId());
        var type = (ClassType) self.getType();
        var copy = new UpdateObjectNode(
                null,
                node.getName(),
                getTransformedNode(node.getPredecessor()),
                scope()
        );
        copy.setObjectId(self);
        for (UpdateField updateField : node.getUpdateFields()) {
            copy.setUpdateField(
                    type.getFieldByName(updateField.getField().getName()),
                    updateField.getOp(),
                    transformValue(updateField.getValue())
            );
        }
        return copy;
    }

    private RaiseNode transformExceptionNode(RaiseNode node) {
        return new RaiseNode(
                null, node.getName(),
                transformValue(node.getException()),
                getTransformedNode(node.getPredecessor()),
                scope()
        );
    }

    private DeleteObjectNode transformDeleteObjectNode(DeleteObjectNode node) {
        var copy = new DeleteObjectNode(
                null, node.getName(), getTransformedNode(node), scope()
        );
        copy.setObjectId(transformValue(node.getObjectId()));
        return copy;
    }

    private MergeNode transformMergeNode(MergeNode node) {
        var type = (ClassType) transformType(node.getType());
        var branchNode = (BranchNode) getTransformedNode(node.getPredecessor());
        var copy = new MergeNode(
                null, node.getName(),
                branchNode,
                type,
                scope()
        );
        for (MergeNodeField field : node.getFields()) {
            var fieldCopy = new MergeNodeField(
                    type.getFieldByName(field.getField().getName()),
                    copy
            );
            for (ConditionalValue value : field.getValues()) {
                var branch = branchNode.getBranchByIndex(value.getBranch().getIndex());
                fieldCopy.setValue(branch, transformValue(value.getValue()));
            }
            copy.addField(fieldCopy);
        }
        return copy;
    }

    private ValueNode transformValueNode(ValueNode valueNode) {
        return new ValueNode(
                null, valueNode.getName(),
                transformType(valueNode.getType()),
                getTransformedNode(valueNode.getPredecessor()),
                scope(), transformValue(valueNode.getValue())
        );
    }

    private GetUniqueNode transformGetUniqueNode(GetUniqueNode node) {
        var type = (ClassType) transformType(node.getConstraint().getDeclaringType());
        var index = (Index) type.getConstraints().get(
                node.getConstraint().getDeclaringType().getConstraints().indexOf(node.getConstraint())
        );
        return new GetUniqueNode(
                null, node.getName(), index,
                getTransformedNode(node.getPredecessor()), scope()
        );
    }

    private UpdateStaticNode transformUpdateStaticNode(UpdateStaticNode node) {
        var copy = new UpdateStaticNode(
                null, node.getName(), getTransformedNode(node.getPredecessor()), scope(),
                node.getUpdateType()
        );
        for (UpdateField field : node.getFields()) {
            copy.setUpdateField(field.getField(), field.getOp(),
                    transformValue(field.getValue()));
        }
        return copy;
    }

    protected ScopeRT scope() {
        return NncUtils.requireNonNull(scopes.peek());
    }

    protected void enterScope(ScopeRT scope) {
        scopes.push(scope);
    }

    protected void exitScope() {
        scopes.pop();
    }

    protected Flow flow() {
        return NncUtils.requireNonNull(flows.peek());
    }

    public void enterFlow(Flow flow) {
        flows.push(flow);
    }

    public void exitFlow() {
        flows.pop();
    }

    private Value transformValue(Value value) {
        return switch (value) {
            case ConstantValue constantValue -> new ConstantValue(
                    transformExpression(constantValue.getExpression())
            );
            case ReferenceValue referenceValue -> new ReferenceValue(
                    transformExpression(referenceValue.getExpression())
            );
            case ExpressionValue expressionValue -> new ExpressionValue(
                    transformExpression(expressionValue.getExpression())
            );
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public Expression transformExpression(Expression expression) {
        return switch (expression) {
            case BinaryExpression binaryExpression -> transformBinaryExpression(binaryExpression);
            case UnaryExpression unaryExpression -> transformUnaryExpression(unaryExpression);
            case FieldExpression fieldExpression -> transformFieldExpression(fieldExpression);
            case ArrayAccessExpression arrayAccExpression -> transformArrayAccessExpression(arrayAccExpression);
            case FunctionExpression functionExpression -> transformFunctionExpression(functionExpression);
            case AsExpression asExpression -> transformAsExpression(asExpression);
            case ConditionalExpression conditionalExpression -> transformConditionalExpression(conditionalExpression);
            case ConstantExpression constantExpression -> transformConstantExpression(constantExpression);
            case VariableExpression variableExpression -> transformVariableExpression(variableExpression);
            case VariablePathExpression variablePathExpression ->
                    transformVariablePathExpression(variablePathExpression);
            case CursorExpression cursorExpression -> transformCursorExpression(cursorExpression);
            case AllMatchExpression allMatchExpression -> transformAllMatchExpression(allMatchExpression);
            case StaticFieldExpression staticFieldExpression -> transformStaticFieldExpression(staticFieldExpression);
            case NodeExpression nodeExpression -> transformNodeExpression(nodeExpression);
            case ThisExpression thisExpression -> transformThisExpression(thisExpression);
            case ArrayExpression arrayExpression -> transformArrayExpression(arrayExpression);
            case InstanceOfExpression instanceOfExpression -> transformInstanceOfExpression(instanceOfExpression);
            default -> throw new IllegalStateException("Unexpected expression: " + expression);
        };
    }

    public Expression transformBinaryExpression(BinaryExpression expression) {
        return new BinaryExpression(
                expression.getOperator(),
                transformExpression(expression.getFirst()),
                transformExpression(expression.getSecond())
        );
    }

    public Expression transformUnaryExpression(UnaryExpression expression) {
        return new UnaryExpression(
                expression.getOperator(),
                transformExpression(expression.getOperand())
        );
    }

    public Expression transformFieldExpression(FieldExpression expression) {
        var transformedSelf = transformExpression(expression.getInstance());
        ClassType selfType = switch (transformedSelf.getType()) {
            case ClassType classType -> classType;
            case TypeVariable typeVariable -> (ClassType) typeVariable.getBounds().get(0);
            default -> throw new IllegalStateException("Unexpected value: " + transformedSelf.getType());
        };
        var field = selfType.getFieldByCode(expression.getField().getCode());
        return new FieldExpression(transformedSelf, field);
    }

    public Expression transformArrayAccessExpression(ArrayAccessExpression expression) {
        return new ArrayAccessExpression(
                transformExpression(expression.getArray()),
                transformExpression(expression.getIndex())
        );
    }

    public Expression transformFunctionExpression(FunctionExpression expression) {
        return new FunctionExpression(
                expression.getFunction(),
                NncUtils.map(expression.getArguments(), this::transformExpression)
        );
    }

    public Expression transformAsExpression(AsExpression expression) {
        return new AsExpression(
                transformExpression(expression.getExpression()),
                expression.getAlias()
        );
    }

    public Expression transformConditionalExpression(ConditionalExpression expression) {
        return new ConditionalExpression(
                transformExpression(expression.getCondition()),
                transformExpression(expression.getTrueValue()),
                transformExpression(expression.getFalseValue())
        );
    }

    public Expression transformConstantExpression(ConstantExpression expression) {
        return new ConstantExpression(expression.getValue());
    }

    public Expression transformVariableExpression(VariableExpression expression) {
        return new VariableExpression(expression.getVariable());
    }

    public Expression transformVariablePathExpression(VariablePathExpression expression) {
        return new VariablePathExpression(
                transformExpression(expression.getQualifier()),
                new VariableExpression(expression.getField().getVariable())
        );
    }

    public Expression transformCursorExpression(CursorExpression expression) {
        return new CursorExpression(
                transformExpression(expression.getArray()),
                expression.getAlias()
        );
    }

    public Expression transformAllMatchExpression(AllMatchExpression expression) {
        return new AllMatchExpression(
                transformExpression(expression.getArray()),
                transformExpression(expression.getCondition()),
                new CursorExpression(
                        transformExpression(expression.getArray()),
                        expression.getCursor().getAlias()
                )
        );
    }

    public Expression transformStaticFieldExpression(StaticFieldExpression expression) {
        return new StaticFieldExpression(expression.getField());
    }

    public Expression transformNodeExpression(NodeExpression expression) {
        return new NodeExpression(getTransformedNode(expression.getNode()));
    }

    public Expression transformThisExpression(ThisExpression expression) {
        return new ThisExpression(currentClass());
    }

    public Expression transformArrayExpression(ArrayExpression expression) {
        return new ArrayExpression(
                NncUtils.map(expression.getExpressions(), this::transformExpression)
        );
    }

    public Expression transformInstanceOfExpression(InstanceOfExpression expression) {
        return new InstanceOfExpression(
                transformExpression(expression.getOperand()), expression.getTargetType()
        );
    }

    protected final NodeRT<?> getTransformedNode(NodeRT<?> node) {
        if (node == null) {
            return null;
        }
        return NncUtils.requireNonNull(nodeMap.get(node));
    }

    protected Flow transformFlowReference(Flow flow) {
        return flow;
    }

}
