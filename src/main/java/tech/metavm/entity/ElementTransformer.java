package tech.metavm.entity;

import tech.metavm.autograph.ExpressionTypeMap;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.flow.Value;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class ElementTransformer {

    private final Map<NodeRT<?>, NodeRT<?>> transformedNodes = new IdentityHashMap<>();
    private final Map<Type, Type> transformedTypes = new IdentityHashMap<>();

    private final LinkedList<ClassType> classes = new LinkedList<>();
    private final LinkedList<Flow> flows = new LinkedList<>();
    private final LinkedList<ScopeRT> scopes = new LinkedList<>();
    protected final TypeFactory typeFactory;
    @Nullable
    private final IEntityContext entityContext;

    public ElementTransformer() {
        this(null);
    }

    public ElementTransformer(IEntityContext entityContext) {
        this(new DefaultTypeFactory(ModelDefRegistry::getType), entityContext);
    }

    public ElementTransformer(TypeFactory typeFactory, @Nullable IEntityContext entityContext) {
        this.typeFactory = typeFactory;
        this.entityContext = entityContext;
    }

    public Type transformType(Type type) {
        return switch (type) {
            case ClassType classType -> transformClassType(classType);
            case ArrayType arrayType -> transformArrayType(arrayType);
            case UnionType unionType -> transformUnionType(unionType);
            case TypeVariable typeVariable -> transformTypeVariable(typeVariable);
            case PrimitiveType primitiveType -> transformPrimitiveType(primitiveType);
            case FunctionType functionType -> transformFunctionType(functionType);
            case UncertainType uncertainType -> transformUncertainType(uncertainType);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public Type transformTypeReference(Type typeReference) {
        var transformed = getTransformedType(typeReference);
        if (transformed != null) {
            return transformed;
        }
        return switch (typeReference) {
            case ClassType classTypeRef -> transformClassTypeReference(classTypeRef);
            case ArrayType arrayTypeRef -> transformArrayTypeReference(arrayTypeRef);
            case UnionType unionTypeRef -> transformUnionTypeReference(unionTypeRef);
            case TypeVariable typeVariableRef -> transformTypeVariableReference(typeVariableRef);
            case PrimitiveType primitiveTypeRef -> transformPrimitiveTypeReference(primitiveTypeRef);
            case FunctionType funcTypeRef -> transformFunctionTypeReference(funcTypeRef);
            case UncertainType uncertainTypeRef -> transformUncertainTypeReference(uncertainTypeRef);
            default -> throw new IllegalStateException("Unexpected value: " + typeReference);
        };
    }

    public Type transformUncertainTypeReference(UncertainType uncertainTypeRef) {
        return transformedTypes.getOrDefault(uncertainTypeRef, uncertainTypeRef);
    }

    public Type transformFunctionTypeReference(FunctionType funcTypeRef) {
        return transformedTypes.getOrDefault(funcTypeRef, funcTypeRef);
    }

    public Type transformPrimitiveTypeReference(PrimitiveType primitiveTypeRef) {
        return transformedTypes.getOrDefault(primitiveTypeRef, primitiveTypeRef);
    }

    public Type transformTypeVariableReference(TypeVariable typeVariableRef) {
        return transformedTypes.getOrDefault(typeVariableRef, typeVariableRef);
    }

    public Type transformUnionTypeReference(UnionType unionTypeRef) {
        return transformedTypes.getOrDefault(unionTypeRef, unionTypeRef);
    }

    public Type transformArrayTypeReference(ArrayType arrayTypeRef) {
        return transformedTypes.getOrDefault(arrayTypeRef, arrayTypeRef);
    }

    public Type transformClassTypeReference(ClassType classTypeReference) {
        return transformedTypes.getOrDefault(classTypeReference, classTypeReference);
    }

    private Type transformUncertainType(UncertainType uncertainType) {
        if (entityContext == null) {
            throw new InternalException("Can not transform UncertainType because UncertainTypeContext is not available");
        }
        return entityContext.getUncertainType(
                transformTypeReference(uncertainType.getLowerBound()),
                transformTypeReference(uncertainType.getUpperBound())
        );
    }

    private FunctionType transformFunctionType(FunctionType functionType) {
        if (entityContext == null) {
            throw new InternalException(
                    "Unable to transform function types because FunctionTypeContext is not available");
        }
        return entityContext.getFunctionType(
                NncUtils.map(functionType.getParameterTypes(), this::transformTypeReference),
                transformTypeReference(functionType.getReturnType())
        );
    }

    public ClassType transformClassType(ClassType classType) {
        return transformClassType(
                classType,
                null,
                classType.getName(),
                classType.getCode(),
                classType.getTemplate(),
                classType.getTypeArguments(),
                classType.isAnonymous()
        );
    }

    protected ClassType transformClassType(ClassType classType,
                                        Long tmpId,
                                        String name,
                                        @Nullable String code,
                                        @Nullable ClassType template,
                                        List<Type> typeArgs,
                                        boolean anonymous
    ) {
        var transformed = ClassBuilder.newBuilder(name, code)
                .tmpId(tmpId)
                .desc(classType.getDesc())
                .existing(getExistingClass())
                .source(ClassSource.RUNTIME)
                .category(classType.getCategory())
                .anonymous(anonymous)
                .template(template)
                .dependencies(NncUtils.map(classType.getDependencies(),
                        dep -> (ClassType) transformTypeReference(dep)))
                .typeArguments(typeArgs)
                .interfaces(NncUtils.map(
                        classType.getInterfaces(), it -> (ClassType) transformTypeReference(it)
                ))
                .superClass(classType.getSuperClass() != null ?
                        (ClassType) transformTypeReference(classType.getSuperClass()) : null)
                .ephemeral(classType.isEphemeral())
                .build();
        transformed.setStage(classType.getStage());
        setTransformedType(classType, transformed);
        onClassTypeTransformed(transformed);
        enterClass(transformed);
        transformClassBody(classType);
        exitClass();
        return transformed;
    }

    @Nullable
    protected ClassType getExistingClass() {
        return null;
    }

    protected void transformClassBody(ClassType classType) {
        classType.getDeclaredFields().forEach(this::transformField);
        classType.getDeclaredFlows().forEach(this::transformFlow);
    }

    protected void onClassTypeTransformed(ClassType transformedClassType) {

    }

    private void setTransformedType(Type original, Type transformed) {
        transformedTypes.put(original, transformed);
    }

    public TypeVariable transformTypeVariable(TypeVariable typeVariable) {
        var transformed = new TypeVariable(null, typeVariable.getName(), typeVariable.getCode(),
                DummyGenericDeclaration.INSTANCE);
        typeFactory.addType(transformed);
        setTransformedType(typeVariable, transformed);
        transformed.setBounds(NncUtils.map(typeVariable.getBounds(), this::transformTypeReference));
        return transformed;
    }

    public Type transformArrayType(ArrayType arrayType) {
        if (entityContext == null) {
            throw new InternalException("Can not transform array type because ArrayTypeContext is not available");
        }
        return entityContext.getArrayType(
                transformTypeReference(arrayType.getElementType()),
                arrayType.getKind()
        );
    }

    public Type transformUnionType(UnionType unionType) {
        if (entityContext == null) {
            throw new InternalException("Can not transform union type because UnionTypeContext is not available");
        }
        return entityContext.getUnionType(NncUtils.mapUnique(unionType.getMembers(), this::transformTypeReference));
    }

    public Type transformPrimitiveType(PrimitiveType primitiveType) {
        return primitiveType;
    }

    public Field transformField(Field field) {
        var typeDTO = currentTypeDTO();
        var fieldDTO = NncUtils.get(typeDTO, t -> t.getClassParam().findFieldByName(field.getName()));
        var transformed = FieldBuilder.newBuilder(
                        field.getName(), field.getCode(), currentClass(), transformTypeReference(field.getType()))
                .tmpId(NncUtils.get(fieldDTO, FieldDTO::tmpId))
                .existing(getExistingField(field))
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

    protected TypeDTO currentTypeDTO() {
        return null;
    }

    protected Field getExistingField(Field templateField) {
        if (!classes.isEmpty()) {
            var klass = currentClass();
            return klass.tryGetFieldByName(templateField.getName());
        } else {
            return null;
        }
    }

    private Flow getExistingFlow(Flow templateFlow) {
        return currentClass().getFlowByRootTemplate(templateFlow);
    }

    public Flow transformFlow(Flow flow) {
        var typeArguments = NncUtils.map(flow.getTypeArguments(), this::transformTypeReference);
        List<TypeVariable> typeParams = NncUtils.map(
                flow.getTypeParameters(), this::transformTypeVariable
        );
        return transformFlow(flow, flow.getTemplate(), flow.getName(), flow.getCode(),
                typeParams, typeArguments);
    }

    public Flow transformFlow(Flow flow, Flow template, String name, String code,
                              List<TypeVariable> typeParameters, List<Type> typeArguments) {
        if (entityContext == null) {
            throw new InternalException(
                    "Unable to transform flow, because functionTypeContext is not provided");
        }
        flow.analyze();

        List<Flow> concreteOverriden = new ArrayList<>();
        for (Flow overridden : flow.getOverridden()) {
            var overridenType = overridden.getDeclaringType();
            var concreteOverridenType =
                    (ClassType) transformTypeReference(flow.getDeclaringType().getAncestorType(overridenType));
            concreteOverriden.add(concreteOverridenType.getFlows().get(
                    overridenType.getFlows().indexOf(overridden)
            ));
        }

        var params = NncUtils.map(flow.getParameters(), param -> transformFlowParameter(flow, param));
        var paramTypes = NncUtils.map(params, Parameter::getType);

        var typeDTO = currentTypeDTO();
        var flowDTO = NncUtils.get(typeDTO, t -> t.getClassParam().findFlowBySignature(
                Flow.getSignature(name, paramTypes)
        ));
        var declaringType = hasCurrentClass() ? currentClass() : flow.getDeclaringType();
        var transformed = FlowBuilder.newBuilder(declaringType, name, code, entityContext.getFunctionTypeContext())
                .template(template)
                .tmpId(NncUtils.get(flowDTO, FlowDTO::tmpId))
                .nullType(typeFactory.getNullType())
                .existing(getExistingFlow(flow))
                .typeArguments(typeArguments)
                .overriden(concreteOverriden)
                .isAbstract(flow.isAbstract())
                .typeParameters(typeParameters)
                .isNative(flow.isNative())
                .isConstructor(flow.isConstructor())
                .parameters(params)
                .returnType(transformTypeReference(flow.getReturnType()))
                .build();

        enterFlow(transformed);
        enterScope(transformed.getRootScope());
        transformed.setRootScope(transformScope(flow.getRootScope()));
        exitScope();
        exitFlow();
        return transformed;
    }

    public Parameter transformFlowParameter(Flow flow, Parameter parameter) {
        var existingParam = getExistingParameter(flow, parameter);
        if (existingParam != null) {
            existingParam.setName(parameter.getName());
            existingParam.setCode(parameter.getCode());
            existingParam.setType(transformTypeReference(parameter.getType()));
            return existingParam;
        } else {
            return transformParameter(parameter);
        }
    }

    public Parameter transformParameter(Parameter parameter) {
        return new Parameter(
                null,
                parameter.getName(),
                parameter.getCode(),
                transformTypeReference(parameter.getType()),
                NncUtils.get(parameter.getCondition(), this::transformValue),
                parameter,
                null
        );
    }

    protected Parameter getExistingParameter(Flow flow, Parameter parameter) {
        var existingFlow = getExistingFlow(flow);
        if (existingFlow != null) {
            return existingFlow.getParameterByName(parameter.getName());
        } else {
            return null;
        }
    }

    public ScopeRT transformScope(ScopeRT scope) {
        var transformed = new ScopeRT(flow(),
                NncUtils.get(scope.getOwner(), this::getTransformedNode), scope.isWithBackEdge());
        transformed.setExpressionTypes(transformExpressionTypeMap(scope.getExpressionTypes()));
        enterScope(transformed);
        scope.getNodes().forEach(this::transformNode);
        exitScope();
        return transformed;
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
        for (ClassType s : getRawType(currentType).getSuperTypes()) {
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

    public NodeRT<?> transformNode(NodeRT<?> node) {
        var transformed = switch (node) {
            case SelfNode selfNode -> transformSelfNode(selfNode);
            case InputNode inputNode -> transformInputNode(inputNode);
            case ReturnNode returnNode -> transformReturnNode(returnNode);
            case BranchNode branchNode -> transformBranchNode(branchNode);
            case WhileNode whileNode -> transformWhileNode(whileNode);
            case UpdateObjectNode updateObjectNode -> transformUpdateObjectNode(updateObjectNode);
            case NewObjectNode newNode -> transformNewNode(newNode);
            case SubFlowNode subFlowNode -> transformSubFlowNode(subFlowNode);
            case RaiseNode exceptionNode -> transformExceptionNode(exceptionNode);
            case ValueNode valueNode -> transformValueNode(valueNode);
            case UpdateStaticNode updateStaticNode -> transformUpdateStaticNode(updateStaticNode);
            case GetUniqueNode getUniqueNode -> transformGetUniqueNode(getUniqueNode);
            case DeleteObjectNode deleteObjectNode -> transformDeleteObjectNode(deleteObjectNode);
            case MergeNode mergeNode -> transformMergeNode(mergeNode);
            case AddObjectNode addObjectNode -> transformAddObjectNode(addObjectNode);
            case CheckNode checkNode -> transformCheckNode(checkNode);
            case LambdaNode lambdaNode -> transformLambdaNode(lambdaNode);
            case NewArrayNode newArrayNode -> transformNewArrayNode(newArrayNode);
            case AddElementNode addElementNode -> transformAddElementNode(addElementNode);
            case DeleteElementNode deleteElementNode -> transformDeleteElementNode(deleteElementNode);
            case GetElementNode getElementNode -> transformGetElementNode(getElementNode);
            default -> throw new IllegalStateException("Unexpected node: " + node);
        };
        if (node.getExpressionTypes() != ExpressionTypeMap.EMPTY) {
            transformed.setExpressionTypes(transformExpressionTypeMap(node.getExpressionTypes()));
        }
        setTransformedNode(node, transformed);
        return transformed;
    }

    public ParentRef transformParentRef(ParentRef parentRef) {
        var parent = transformValue(parentRef.getParent());
        return new ParentRef(
                parent,
                NncUtils.get(parentRef.getField(),
                        f -> transformFieldReference((ClassType) parent.getType(), f))
        );
    }

    public ExpressionTypeMap transformExpressionTypeMap(ExpressionTypeMap expressionTypeMap) {
        Map<Expression, Type> transformedMap = new IdentityHashMap<>();
        expressionTypeMap.toMap().forEach((expr, type) ->
                transformedMap.put(transformExpression(expr), transformTypeReference(type))
        );
        return new ExpressionTypeMap(transformedMap);
    }

    public GetElementNode transformGetElementNode(GetElementNode node) {
        return new GetElementNode(
                null, node.getName(),
                getTransformedNode(node.getPredecessor()),
                scope(),
                transformValue(node.getArray()),
                transformValue(node.getIndex())
        );
    }

    public AddElementNode transformAddElementNode(AddElementNode node) {
        return new AddElementNode(
                null, node.getName(),
                getTransformedNode(node.getPredecessor()),
                scope(),
                transformValue(node.getArray()),
                transformValue(node.getElement())
        );
    }

    public DeleteElementNode transformDeleteElementNode(DeleteElementNode node) {
        return new DeleteElementNode(
                null, node.getName(),
                getTransformedNode(node.getPredecessor()),
                scope(),
                transformValue(node.getArray()),
                transformValue(node.getElement())
        );
    }

    public NewArrayNode transformNewArrayNode(NewArrayNode node) {
        return new NewArrayNode(
                null, node.getName(),
                (ArrayType) transformTypeReference(node.getType()),
                transformValue(node.getValue()),
                NncUtils.get(node.getParentRef(), this::transformParentRef),
                getTransformedNode(node.getPredecessor()),
                scope()
        );
    }

    public LambdaNode transformLambdaNode(LambdaNode node) {
        var transformed = new LambdaNode(
                null,
                node.getName(),
//                transformType(node.getType()),
                getTransformedNode(node.getPredecessor()),
                scope(),
                NncUtils.map(node.getParameters(), this::transformParameter),
                transformTypeReference(node.getReturnType()),
                (FunctionType) transformTypeReference(node.getFunctionType()),
                NncUtils.get(node.getFunctionalInterface(), funcInterface -> (ClassType) transformTypeReference(funcInterface))
        );
        transformed.setExpressionTypes(transformExpressionTypeMap(node.getExpressionTypes()));
        setTransformedNode(node, transformed);
        enterScope(transformed.getBodyScope());
        node.getBodyScope().getNodes().forEach(this::transformNode);
        exitScope();
        return transformed;
    }

    public CheckNode transformCheckNode(CheckNode checkNode) {
        return new CheckNode(
                null, checkNode.getName(), getTransformedNode(checkNode.getPredecessor()),
                scope(), transformValue(checkNode.getCondition()),
                (BranchNode) getTransformedNode(checkNode.getExit()
                )
        );
    }

    public AddObjectNode transformAddObjectNode(AddObjectNode addObjectNode) {
        var type = (ClassType) transformTypeReference(addObjectNode.getType());
        var transformed = new AddObjectNode(null, addObjectNode.getName(),
                addObjectNode.isInitializeArrayChildren(), type,
                getTransformedNode(addObjectNode.getPredecessor()),
                scope()
        );
        for (FieldParam field : addObjectNode.getFields()) {
            transformed.addField(
                    new FieldParam(transformFieldReference(type, field.getField()),
                            transformValue(field.getValue()))
            );
        }
        return transformed;
    }

    public SelfNode transformSelfNode(SelfNode selfNode) {
        return new SelfNode(null, selfNode.getName(),
                (ClassType) transformTypeReference(selfNode.getType()),
                getTransformedNode(selfNode.getPredecessor()),
                scope());
    }

    public InputNode transformInputNode(InputNode inputNode) {
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

    public ReturnNode transformReturnNode(ReturnNode returnNode) {
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

    public BranchNode transformBranchNode(BranchNode branchNode) {
        var transformed = new BranchNode(
                null,
                branchNode.getName(),
                branchNode.isInclusive(),
                getTransformedNode(branchNode.getPredecessor()),
                scope()
        );
        transformed.setExpressionTypes(transformExpressionTypeMap(branchNode.getExpressionTypes()));
        setTransformedNode(branchNode, transformed);
        for (Branch branch : branchNode.getBranches()) {
            var branchCopy = new Branch(
                    branch.getIndex(),
                    transformValue(branch.getCondition()),
                    branch.isPreselected(),
                    branch.isExit(),
                    new ScopeRT(flow(), transformed),
                    transformed
            );
            transformed.addBranch(branchCopy);
            branchCopy.getScope().setExpressionTypes(transformExpressionTypeMap(branch.getScope().getExpressionTypes()));
            enterScope(branchCopy.getScope());
            branch.getScope().getNodes().forEach(this::transformNode);
            exitScope();
        }
        return transformed;
    }

    public WhileNode transformWhileNode(WhileNode node) {
        var transformed = new WhileNode(
                null,
                node.getName(),
                (ClassType) transformTypeReference(node.getType()),
                getTransformedNode(node.getPredecessor()),
                scope(),
                new ExpressionValue(ExpressionUtil.trueExpression())
        );
        transformed.setExpressionTypes(transformExpressionTypeMap(node.getExpressionTypes()));
        setTransformedNode(node, transformed);
        enterScope(transformed.getBodyScope());
        node.getBodyScope().getNodes().forEach(this::transformNode);
        exitScope();
        transformed.setCondition(transformValue(node.getCondition()));
        for (LoopField field : node.getFields()) {
            transformed.setField(
                    transformed.getType().tryGetFieldByName(field.getField().getName()),
                    transformValue(field.getInitialValue()),
                    transformValue(field.getUpdatedValue())
            );
        }
        return transformed;
    }

    public SubFlowNode transformSubFlowNode(SubFlowNode subFlowNode) {
        var transformedSelf = transformValue(subFlowNode.getSelfId());
        var selfType = getConcreteClassType(getExpressionType(transformedSelf.getExpression()));
//        var subFlow = selfType.getFlows().get(
//                subFlowNode.getSubFlow().getDeclaringType().getFlows().indexOf(subFlowNode.getSubFlow())
//        );
//        subFlow = selfType.getFlows().get(
//                subFlow.getDeclaringType().getFlows().indexOf(subFlow)
//        );
        var transformedSubFlow = transformFlowReference(selfType, subFlowNode.getSubFlow());
        List<Argument> arguments = new ArrayList<>();
        NncUtils.biForEach(
                subFlowNode.getArguments(),
                transformedSubFlow.getParameters(),
                (arg, param) -> arguments.add(new Argument(null, param, transformValue(arg.getValue())))
        );
        return new SubFlowNode(
                null,
                subFlowNode.getName(),
                getTransformedNode(subFlowNode.getPredecessor()),
                scope(),
                transformedSelf,
                arguments,
                transformedSubFlow
        );
    }

    public NewObjectNode transformNewNode(NewObjectNode node) {
        var type = (ClassType) transformTypeReference(node.getType());
//        var subFlow = type.getFlows().get(
//                node.getSubFlow().getDeclaringType().getFlows().indexOf(node.getSubFlow())
//        );
        var transformedSubFlow = transformFlowReference(type, node.getSubFlow());
        List<Argument> arguments = new ArrayList<>();
        NncUtils.biForEach(
                node.getArguments(),
                transformedSubFlow.getParameters(),
                (arg, param) -> arguments.add(new Argument(null, param, transformValue(arg.getValue())))
        );
        return new NewObjectNode(
                null,
                node.getName(),
                transformedSubFlow,
                arguments,
                getTransformedNode(node.getPredecessor()),
                scope(),
                NncUtils.get(node.getParentRef(), this::transformParentRef)
        );
    }

    public UpdateObjectNode transformUpdateObjectNode(UpdateObjectNode node) {
        var self = transformValue(node.getObjectId());
        var type = (ClassType) getExpressionType(self.getExpression());
        var copy = new UpdateObjectNode(
                null,
                node.getName(),
                getTransformedNode(node.getPredecessor()),
                scope()
        );
        copy.setObjectId(self);
        for (UpdateField updateField : node.getUpdateFields()) {
            copy.setUpdateField(
                    type.tryGetFieldByName(updateField.getField().getName()),
                    updateField.getOp(),
                    transformValue(updateField.getValue())
            );
        }
        return copy;
    }

    public RaiseNode transformExceptionNode(RaiseNode node) {
        return new RaiseNode(
                null, node.getName(),
                node.getParameterKind(),
                NncUtils.get(node.getException(), this::transformValue),
                NncUtils.get(node.getMessage(), this::transformValue),
                getTransformedNode(node.getPredecessor()),
                scope()
        );
    }

    public DeleteObjectNode transformDeleteObjectNode(DeleteObjectNode node) {
        var copy = new DeleteObjectNode(
                null, node.getName(), getTransformedNode(node.getPredecessor()), scope()
        );
        copy.setObjectId(transformValue(node.getObjectId()));
        return copy;
    }

    public MergeNode transformMergeNode(MergeNode node) {
        var type = (ClassType) transformType(node.getType());
        var branchNode = (BranchNode) NncUtils.requireNonNull(getTransformedNode(node.getPredecessor()));
        var copy = new MergeNode(
                null, node.getName(),
                branchNode,
                type,
                scope()
        );
        for (MergeNodeField field : node.getFields()) {
            var fieldCopy = new MergeNodeField(
                    type.tryGetFieldByName(field.getField().getName()),
                    copy
            );
            for (ConditionalValue value : field.getValues()) {
                var branch = branchNode.getBranchByIndex(value.getBranch().getIndex());
                fieldCopy.setValue(branch, transformValue(value.getValue()));
            }
        }
        return copy;
    }

    public ValueNode transformValueNode(ValueNode valueNode) {
        return new ValueNode(
                null, valueNode.getName(),
                transformTypeReference(valueNode.getType()),
                getTransformedNode(valueNode.getPredecessor()),
                scope(), transformValue(valueNode.getValue())
        );
    }

    public GetUniqueNode transformGetUniqueNode(GetUniqueNode node) {
        var type = (ClassType) transformTypeReference(node.getConstraint().getDeclaringType());
        var index = (Index) type.getConstraints().get(
                node.getConstraint().getDeclaringType().getConstraints().indexOf(node.getConstraint())
        );
        return new GetUniqueNode(
                null, node.getName(), index,
                getTransformedNode(node.getPredecessor()), scope()
        );
    }

    public UpdateStaticNode transformUpdateStaticNode(UpdateStaticNode node) {
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

    private tech.metavm.flow.Value transformValue(Value value) {
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
            case PropertyExpression fieldExpression -> transformPropertyExpression(fieldExpression);
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

    private ClassType getConcreteClassType(Type type) {
        var bound = type.getUltimateUpperBound();
        if (bound instanceof ClassType classType) {
            return classType;
        } else {
            throw new IllegalStateException("type " + type + " doesn't have a class upper bound");
        }
    }

    public Expression transformPropertyExpression(PropertyExpression expression) {
        var transformedSelf = transformExpression(expression.getInstance());
        ClassType selfType = getConcreteClassType(getExpressionType(transformedSelf));
        var property = transformPropertyReference(selfType, expression.getProperty());
        return new PropertyExpression(transformedSelf, property);
    }

    private Type getExpressionType(Expression expression) {
        var lastNode = scope().getLastNode();
        if (lastNode == null) {
            return scope().getExpressionTypes().getType(expression);
        } else {
            return lastNode.getExpressionTypes().getType(expression);
        }
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
                NncUtils.map(expression.getExpressions(), this::transformExpression),
                (ArrayType) transformTypeReference(expression.getType())
        );
    }

    public Expression transformInstanceOfExpression(InstanceOfExpression expression) {
        return new InstanceOfExpression(
                transformExpression(expression.getOperand()), expression.getTargetType()
        );
    }

    private void setTransformedNode(NodeRT<?> original, NodeRT<?> transformed) {
        transformedNodes.put(original, transformed);
    }

    protected final NodeRT<?> getTransformedNode(NodeRT<?> node) {
        if (node == null) {
            return null;
        }
        return NncUtils.requireNonNull(transformedNodes.get(node));
    }

    protected final Type getTransformedType(Type type) {
        return transformedTypes.get(type);
    }

    public Property transformPropertyReference(ClassType declaringType, Property property) {
        return switch (property) {
            case Field field -> transformFieldReference(declaringType, field);
            case Flow flow -> transformFlowReference(declaringType, flow);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    public final Field transformFieldReference(ClassType declaringType, Field field) {
        return declaringType.tryGetFieldByName(field.getName());
    }

    public Flow transformFlowReference(ClassType declaringType, Flow flow) {
        return declaringType.getFlow(flow.getName(),
                NncUtils.map(flow.getParameterTypes(), this::transformTypeReference));
    }


}
