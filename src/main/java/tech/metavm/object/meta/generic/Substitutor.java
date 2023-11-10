package tech.metavm.object.meta.generic;

import tech.metavm.autograph.ExpressionTypeMap;
import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.Element;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.expression.StaticFieldExpression;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.GenericDeclarationDTO;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.task.FieldData;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.object.meta.ResolutionStage.*;

public class Substitutor extends ElementVisitor<Element> {

    private final GenericContext context;
    private final TypeArgumentMap typeArgumentMap;
    private final TypeFactory typeFactory;
    private final LinkedList<Element> elements = new LinkedList<>();
    private final SaveTypeBatch batch;
    private final ResolutionStage stage;
    private final Map<Type, Type> typeMap = new IdentityHashMap<>();
    private final Map<NodeRT<?>, NodeRT<?>> nodeMap = new IdentityHashMap<>();

    public Substitutor(GenericContext context,
                       List<TypeVariable> typeParameters,
                       List<? extends Type> typeArguments,
                       TypeFactory typeFactory,
                       ResolutionStage stage,
                       SaveTypeBatch batch
    ) {
        typeArgumentMap = new TypeArgumentMap(NncUtils.zip(typeParameters, typeArguments));
        this.typeFactory = typeFactory;
        this.context = context;
        this.stage = stage;
        this.batch = batch;
    }

    private final SubstitutorBase<Type> typeSubstitutor = new SubstitutorBase<>() {

        @Override
        public Type visitType(Type type) {
            return type;
        }

        @Override
        public Type visitTypeVariable(TypeVariable typeVariable) {
            var transformed = new TypeVariable(null, typeVariable.getName(), typeVariable.getCode(),
                    DummyGenericDeclaration.INSTANCE);
            typeFactory.addType(transformed);
            transformed.setBounds(NncUtils.map(typeVariable.getBounds(), t -> substituteType(t)));
            return super.visitTypeVariable(transformed);
        }

        @Override
        public Type visitArrayType(ArrayType arrayType) {
            return super.visitArrayType(context.getEntityContext().getArrayType(
                    substituteType(arrayType.getElementType()),
                    arrayType.getKind()
            ));
        }

        @Override
        public Type visitUnionType(UnionType unionType) {
            return super.visitUnionType(context.getEntityContext().getUnionType(
                    NncUtils.mapUnique(unionType.getMembers(), t -> substituteType(t))
            ));
        }

        @Override
        public Type visitPrimitiveType(PrimitiveType primitiveType) {
            return super.visitPrimitiveType(primitiveType);
        }

        @Override
        public ClassType visitClassType(ClassType classType) {
            var typeArgs = NncUtils.map(classType.getTypeParameters(), typeArgumentMap::get);
            var typeDTO = batch.getTypeDTO(ParameterizedTypeKey.create(classType, typeArgs));
            var existing = context.getExisting(classType, typeArgs);

            var transformed = ClassBuilder
                    .newBuilder(TypeUtils.getParameterizedName(classType.getName(), typeArgs),
                            TypeUtils.getParameterizedName(classType.getCode(), typeArgs))
                    .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                    .existing(existing)
                    .build();
            context.addType(transformed);
            if (stage.isAfterOrAt(SIGNATURE) && transformed.getStage().isBefore(SIGNATURE)) {
                if (classType.getSuperClass() != null)
                    transformed.setSuperClass((ClassType) substituteType(classType.getSuperClass()));
                transformed.setInterfaces(
                        NncUtils.map(classType.getInterfaces(), it -> substituteClassType(it))
                );
                transformed.setDependencies(NncUtils.map(
                        classType.getDependencies(), dep -> substituteClassType(dep)
                ));
            }
            if (stage.isAfterOrAt(DECLARATION) && transformed.getStage().isBefore(/* Not typo */DEFINITION)) {
                enterElement(transformed);
                classType.getFields().forEach(f -> f.accept(this));
                classType.getFlows().forEach(f -> substituteFlow(f));
                classType.getConstraints().forEach(c -> c.accept(this));
                exitElement();
            }
            transformed.setStage(stage);
            return transformed;
        }
    };

    private Type doSubstitution(Type type) {
        var transformed = type.accept(typeSubstitutor);
        typeMap.put(type, transformed);
        return transformed;
    }

    @Override
    public Element visitElement(Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element visitCheckConstraint(CheckConstraint checkConstraint) {
        return new CheckConstraint(
                checkConstraint.getCondition().accept(valueSubstitutor),
                currentClass(),
                checkConstraint.getMessage()
        );
    }

    @Override
    public Element visitIndex(Index index) {
        var transformed = new Index(
                currentClass(),
                index.isUnique(),
                index.getMessage()
        );
        for (IndexField field : index.getFields()) {
            new IndexField(
                    transformed,
                    field.getName(),
                    field.getValue().accept(valueSubstitutor)
            );
        }
        return transformed;
    }

    private final SubstitutorBase<Field> fieldSubstitutor = new SubstitutorBase<>() {
        @Override
        public Field visitField(Field field) {
            var existing = currentClass().findField(f -> f.getTemplate() == field);
            var transformed = FieldBuilder
                    .newBuilder(field.getName(), field.getName(),
                            currentClass(), substituteType(field.getType()))
                    .existing(existing)
                    .isChild(field.isChildField())
                    .unique(field.isUnique())
                    .template(field)
                    .nullType(typeFactory.getNullType())
                    .column(field.getColumn())
                    .isStatic(field.isStatic())
                    .access(field.getAccess())
                    .asTitle(field.isAsTitle())
                    .build();
            return transformed;
        }
    };

    public Flow substituteFlow(Flow flow) {
        return flow.accept(flowSubstitutor);
    }

    private final SubstitutorBase<Flow> flowSubstitutor = new SubstitutorBase<>() {

        @Override
        public Flow visitFlow(Flow flow) {
            var declaringType = currentClass();
            var declaringTypeDTO = batch.getTypeDTO(currentClass().getRef());
            var paramTypes = NncUtils.map(flow.getParameterTypes(), t -> substituteType(t));
            var returnType = substituteType(flow.getReturnType());
            var funcContext = context.getEntityContext().getFunctionTypeContext();
            var existing = declaringType.findFlow(f -> f.getTemplate() == flow);
            var signature = Flow.getSignature(flow.getName(), paramTypes);
            var flowDTO = NncUtils.get(declaringTypeDTO, t -> t.getClassParam().findFlowBySignature(signature));

            List<Flow> transformedOverridden = new ArrayList<>();
            for (Flow overridden : flow.getOverridden()) {
                var overridenType = overridden.getDeclaringType();
                var concreteOverridenType =
                        (ClassType) substituteType(flow.getDeclaringType().getAncestorType(overridenType));
                transformedOverridden.add(concreteOverridenType.getFlows().get(
                        overridenType.getFlows().indexOf(overridden)
                ));
            }

            var transformed = FlowBuilder
                    .newBuilder(declaringType, flow.getName(), flow.getCode(),
                            context.getEntityContext().getFunctionTypeContext())
                    .tmpId(NncUtils.get(flowDTO, FlowDTO::tmpId))
                    .nullType(typeFactory.getNullType())
                    .existing(existing)
                    .returnType(substituteType(flow.getReturnType()))
                    .isConstructor(flow.isConstructor())
                    .parameters(NncUtils.map(flow.getParameters(), p -> p.accept(parameterSubstitutor)))
                    .overriden(transformedOverridden)
                    .type(funcContext.get(paramTypes, returnType))
                    .staticType(funcContext.get(NncUtils.prepend(declaringType, paramTypes), returnType))
                    .build();
            if (stage.isAfterOrAt(DEFINITION)) {
                enterElement(transformed);
                processScope(transformed.getRootScope(), flow.getRootScope());
                exitElement();
            }
            return transformed;
        }

    };


    public void enterElement(Element element) {
        elements.push(element);
    }

    public void exitElement() {
        elements.pop();
    }

    private BaseDTO getElementDTO(Element element) {
        if (element instanceof ClassType classType)
            return batch.getTypeDTO(classType.getRef());
        else if (element instanceof Flow flow)
            return getFlowDTO(flow);
        return null;
    }

    private FlowDTO getFlowDTO(Flow flow) {
        var typeDTO = batch.getTypeDTO(flow.getDeclaringType().getRef());
        if (typeDTO != null) {
            return typeDTO.getClassParam().findFlowBySignature(flow.getSignature());
        }
        return null;
    }

    @Override
    public Element visitTypeVariable(TypeVariable type) {
        var enclosingElement = enclosingElement(List.of(ClassType.class, Flow.class));
        int index = type.getGenericDeclaration().getTypeParameters().indexOf(type);
        var ref = NncUtils.get(getElementDTO(enclosingElement),
                dto -> ((GenericDeclarationDTO) dto).typeParameterRefs().get(index));
        var transformed = NncUtils.get(ref, context.getEntityContext()::getTypeVariable);
        if (transformed == null) {
            transformed = new TypeVariable(
                    NncUtils.get(ref, RefDTO::tmpId),
                    type.getName(),
                    type.getCode(),
                    (GenericDeclaration) enclosingElement
            );
        } else {
            type.setName(type.getName());
            type.setCode(type.getCode());
        }
        type.setBounds(NncUtils.map(type.getBounds(), this::substituteType));
        return transformed;
    }

    final ElementVisitor<Parameter> parameterSubstitutor = new SubstitutorBase<>() {

        @Override
        public Parameter visitParameter(Parameter parameter) {
            var flow = currentFlow();
            var flowDTO = getFlowDTO(currentFlow());
            var transformed = flow.getParameterByName(parameter.getName());
            var paramDTO = NncUtils.get(flowDTO, f -> f.findParameterByName(parameter.getName()));
            if (transformed == null) {
                transformed = new Parameter(
                        NncUtils.get(paramDTO, ParameterDTO::tmpId),
                        parameter.getName(),
                        parameter.getCode(),
                        substituteType(parameter.getType()),
                        null,
                        parameter
                );
            } else {
                transformed.setName(parameter.getName());
                transformed.setCode(parameter.getCode());
                transformed.setType(substituteType(parameter.getType()));
            }
            return transformed;
        }

    };

    private ClassType currentClass() {
        return enclosingElement(ClassType.class);
    }

    private Flow currentFlow() {
        return enclosingElement(Flow.class);
    }

    private Callable currentCallable() {
        return enclosingElement(Callable.class);
    }

    private <T> T enclosingElement(Class<T> elementClass) {
        return elementClass.cast(enclosingElement(List.of(elementClass)));
    }

    private Element enclosingElement(List<Class<?>> elementClasses) {
        for (Element element : elements) {
            for (Class<?> elementClass : elementClasses) {
                if (elementClass.isInstance(elementClass))
                    return element;
            }
        }
        throw new InternalException("No enclosing element of type '" +
                NncUtils.join(elementClasses, Class::getName) + "' is found");
    }

    public ClassType substituteClassType(ClassType classType) {
        return (ClassType) substituteType(classType);
    }

    public Field substituteField(Field field) {
        return field.accept(fieldSubstitutor);
    }

    public Type substituteType(Type type) {
        if (type instanceof TypeVariable typeVariable) {
            return typeArgumentMap.get(typeVariable);
        }
        var transformed = typeMap.get(type);
        if (transformed != null)
            return transformed;
        var variables = type.getVariables();
        if (NncUtils.anyMatch(variables, v -> typeArgumentMap.get(v) != v)) {
            return doSubstitution(type);
        } else {
            return type;
        }
    }

    private Property substituteProperty(ClassType declaringType, Property property) {
        return switch (property) {
            case Field field -> substituteField(declaringType, field);
            case Flow flow -> substituteFlow(declaringType, flow);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    private Flow substituteFlow(ClassType declaringType, Flow flow) {
        return declaringType.getFlow(flow.getName(),
                NncUtils.map(flow.getParameterTypes(), this::substituteType));
    }

    private Field substituteField(ClassType declaringType, Field field) {
        return declaringType.tryGetFieldByName(field.getName());
    }

    private final ElementVisitor<Value> valueSubstitutor = new SubstitutorBase<>() {

        @Override
        public Value visitValue(Value value) {
            return value.substituteExpression(value.getExpression().accept(expressionSubstitutor));
        }

    };


    public ScopeRT currentScope() {
        return enclosingElement(ScopeRT.class);
    }

    private final SubstitutorBase<ParentRef> parentRefSubstitutor = new SubstitutorBase<>() {

        @Override
        public ParentRef visitParentRef(ParentRef parentRef) {
            var parent = parentRef.getParent().accept(valueSubstitutor);
            return new ParentRef(
                    parent,
                    NncUtils.get(parentRef.getField(), f -> substituteField((ClassType) parent.getType(), f))
            );
        }

    };

    public ExpressionTypeMap transformExpressionTypeMap(ExpressionTypeMap expressionTypeMap) {
        Map<Expression, Type> transformedMap = new IdentityHashMap<>();
        expressionTypeMap.toMap().forEach((expr, type) ->
                transformedMap.put(expr.accept(expressionSubstitutor), substituteType(type))
        );
        return new ExpressionTypeMap(transformedMap);
    }

    private void processScope(ScopeRT newScope, ScopeRT originalScope) {
        enterElement(newScope);
        originalScope.getNodes().forEach(this::transformNode);
        exitElement();
    }

    @Override
    public Element visitBranch(Branch branch) {
        var branchNode = (BranchNode) NncUtils.requireNonNull(currentScope().getLastNode());
        var transformed = new Branch(
                branch.getIndex(),
                branch.getCondition().accept(valueSubstitutor),
                branch.isPreselected(),
                branch.isExit(),
                new ScopeRT(currentFlow(), branchNode),
                branchNode
        );
        branchNode.addBranch(transformed);
        transformed.getScope().setExpressionTypes(transformExpressionTypeMap(branch.getScope().getExpressionTypes()));
        processScope(transformed.getScope(), branch.getScope());
        return transformed;
    }

    private void transformNode(NodeRT<?> node) {
        nodeMap.put(node, node.accept(nodeSubstitutor));
    }

    private NodeRT<?> substituteNode(NodeRT<?> node) {
        return NncUtils.requireNonNull(nodeMap.get(node), "Can not substitute node " + node);
    }

    private final SubstitutorBase<NodeRT<?>> nodeSubstitutor = new SubstitutorBase<>() {

        public NodeRT<?> visitGetElementNode(GetElementNode node) {
            return new GetElementNode(
                    null, node.getName(),
                    currentScope().getLastNode(),
                    currentScope(),
                    node.getArray().accept(valueSubstitutor),
                    node.getIndex().accept(valueSubstitutor)
            );
        }

        public NodeRT<?> visitAddElementNode(AddElementNode node) {
            return new AddElementNode(
                    null, node.getName(),
                    currentScope().getLastNode(),
                    currentScope(),
                    node.getArray().accept(valueSubstitutor),
                    node.getElement().accept(valueSubstitutor)
            );
        }

        public NodeRT<?> visitDeleteElementNode(DeleteElementNode node) {
            return new DeleteElementNode(
                    null, node.getName(),
                    currentScope().getLastNode(),
                    currentScope(),
                    node.getArray().accept(valueSubstitutor),
                    node.getElement().accept(valueSubstitutor)
            );
        }

        public NodeRT<?> visitNewArrayNode(NewArrayNode node) {
            return new NewArrayNode(
                    null, node.getName(),
                    (ArrayType) substituteType(node.getType()),
                    node.getValue().accept(valueSubstitutor),
                    NncUtils.get(node.getParentRef(), p -> p.accept(parentRefSubstitutor)),
                    currentScope().getLastNode(),
                    currentScope()
            );
        }

        public NodeRT<?> visitLambdaNode(LambdaNode node) {
            var transformed = new LambdaNode(
                    null,
                    node.getName(),
                    currentScope().getLastNode(),
                    currentScope(),
                    NncUtils.map(node.getParameters(), p -> p.accept(parameterSubstitutor)),
                    substituteType(node.getReturnType()),
                    (FunctionType) substituteType(node.getFunctionType()),
                    NncUtils.get(node.getFunctionalInterface(), funcInterface -> (ClassType) substituteType(funcInterface))
            );
            transformed.setExpressionTypes(transformExpressionTypeMap(node.getExpressionTypes()));
            processScopeNode(transformed, node);
            return transformed;
        }

        private ScopeNode<?> processScopeNode(ScopeNode<?> transformed, ScopeNode<?> original) {
            processScope(transformed.getBodyScope(), original.getBodyScope());
            return transformed;
        }

        public NodeRT<?> visitCheckNode(CheckNode checkNode) {
            return new CheckNode(
                    null, checkNode.getName(),
                    currentScope().getLastNode(),
                    currentScope(),
                    checkNode.getCondition().accept(valueSubstitutor),
                    (BranchNode) substituteNode(checkNode.getExit())
            );
        }

        public NodeRT<?> visitAddObjectNode(AddObjectNode node) {
            var type = (ClassType) substituteType(node.getType());
            var transformed = new AddObjectNode(null, node.getName(),
                    node.isInitializeArrayChildren(), type,
                    currentScope().getLastNode(),
                    currentScope()
            );
            for (FieldParam field : node.getFields()) {
                transformed.addField(
                        new FieldParam(
                                substituteField(type, field.getField()),
                                field.getValue().accept(valueSubstitutor)
                        )
                );
            }
            processScopeNode(transformed, node);
            return transformed;
        }

        public NodeRT<?> visitSelfNode(SelfNode selfNode) {
            return new SelfNode(null, selfNode.getName(),
                    (ClassType) substituteType(selfNode.getType()),
                    currentScope().getLastNode(),
                    currentScope());
        }

        public NodeRT<?> visitInputNode(InputNode inputNode) {
            var inputType = ClassBuilder.newBuilder("输入类型", "InputType")
                    .temporary().build();
            for (Parameter parameter : enclosingElement(Callable.class).getParameters()) {
                FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType, parameter.getType())
                        .build();
            }
            return new InputNode(
                    null,
                    inputNode.getName(),
                    inputType,
                    currentScope().getLastNode(),
                    currentScope()
            );
        }

        public NodeRT<?> visitReturnNode(ReturnNode returnNode) {
            var transformed = new ReturnNode(
                    null,
                    returnNode.getName(),
                    currentScope().getLastNode(),
                    currentScope()
            );
            if (returnNode.getValue() != null) {
                transformed.setValue(returnNode.getValue().accept(valueSubstitutor));
            }
            return transformed;
        }

        public NodeRT<?> visitBranchNode(BranchNode branchNode) {
            var transformed = new BranchNode(
                    null,
                    branchNode.getName(),
                    branchNode.isInclusive(),
                    currentScope().getLastNode(),
                    currentScope()
            );
            transformed.setExpressionTypes(transformExpressionTypeMap(branchNode.getExpressionTypes()));
            for (Branch branch : branchNode.getBranches()) {
                branch.accept(Substitutor.this);
            }
            return transformed;
        }

        public NodeRT<?> visitWhileNode(WhileNode node) {
            var transformed = new WhileNode(
                    null,
                    node.getName(),
                    (ClassType) substituteType(node.getType()),
                    currentScope().getLastNode(),
                    currentScope(),
                    new ExpressionValue(ExpressionUtil.trueExpression())
            );
            transformed.setExpressionTypes(transformExpressionTypeMap(node.getExpressionTypes()));
            processScope(transformed.getBodyScope(), node.getBodyScope());
            transformed.setCondition(node.getCondition().accept(valueSubstitutor));
            for (LoopField field : node.getFields()) {
                transformed.setField(
                        transformed.getType().tryGetFieldByName(field.getField().getName()),
                        field.getInitialValue().accept(valueSubstitutor),
                        field.getUpdatedValue().accept(valueSubstitutor)
                );
            }
            processScopeNode(transformed, node);
            return transformed;
        }

        private NodeRT<?> getPrevNode() {
            return currentScope().getLastNode();
        }

        @Override
        public NodeRT<?> visitTryNode(TryNode node) {
            return processScopeNode(
                    new TryNode(
                            null, node.getName(),
                            getPrevNode(),
                            currentScope()
                    ),
                    node
            );
        }

        @Override
        public NodeRT<?> visitTryEndNode(TryEndNode node) {
            var transformed = new TryEndNode(
                    null, node.getName(),
                    (ClassType) doSubstitution(node.getType()),
                    (TryNode) getPrevNode(),
                    currentScope()
            );
            for (TryEndField field : transformed.getFields()) {
                new TryEndField(
                        substituteField(transformed.getType(), field.getField()),
                        NncUtils.map(
                                field.getValues(),
                                v -> new TryEndValue(
                                        substituteNode(v.getRaiseNode()),
                                        v.getValue().accept(valueSubstitutor)
                                )
                        ),
                        field.getDefaultValue().accept(valueSubstitutor),
                        transformed
                );
            }
            return transformed;
        }

        public NodeRT<?> visitSubFlowNode(SubFlowNode subFlowNode) {
            var transformedSelf = subFlowNode.getSelfId().accept(valueSubstitutor);
            var selfType = getClassBound(getExpressionType(transformedSelf.getExpression()));
//        var subFlow = selfType.getFlows().get(
//                subFlowNode.getSubFlow().getDeclaringType().getFlows().indexOf(subFlowNode.getSubFlow())
//        );
//        subFlow = selfType.getFlows().get(
//                subFlow.getDeclaringType().getFlows().indexOf(subFlow)
//        );
            var transformedSubFlow = substituteFlow(selfType, subFlowNode.getSubFlow());
            List<Argument> arguments = new ArrayList<>();
            NncUtils.biForEach(
                    subFlowNode.getArguments(),
                    transformedSubFlow.getParameters(),
                    (arg, param) -> arguments.add(new Argument(null, param, arg.getValue().accept(valueSubstitutor)))
            );
            return new SubFlowNode(
                    null,
                    subFlowNode.getName(),
                    currentScope().getLastNode(),
                    currentScope(),
                    transformedSelf,
                    arguments,
                    transformedSubFlow
            );
        }

        private ClassType getClassBound(Type type) {
            var bound = type.getUltimateUpperBound();
            if (bound instanceof ClassType classType) {
                return classType;
            } else {
                throw new IllegalStateException("type " + type + " doesn't have a class upper bound");
            }
        }

        private Type getExpressionType(Expression expression) {
            var lastNode = currentScope().getLastNode();
            if (lastNode == null) {
                return currentScope().getExpressionTypes().getType(expression);
            } else {
                return lastNode.getExpressionTypes().getType(expression);
            }
        }

        @Override
        public NodeRT<?> visitNewObjectNode(NewObjectNode node) {
            var type = (ClassType) substituteType(node.getType());
            var transformedSubFlow = substituteFlow(type, node.getSubFlow());
            List<Argument> arguments = new ArrayList<>();
            NncUtils.biForEach(
                    node.getArguments(),
                    transformedSubFlow.getParameters(),
                    (arg, param) -> arguments.add(
                            arg.accept(argumentTransformer)
                            /*new Argument(null, param, arg.getValue().accept(valueTransformer)))*/)
            );
            return new NewObjectNode(
                    null,
                    node.getName(),
                    transformedSubFlow,
                    arguments,
                    currentScope().getLastNode(),
                    currentScope()
            );
        }

        @Override
        public NodeRT<?> visitUpdateObjectNode(UpdateObjectNode node) {
            var self = node.getObjectId().accept(valueSubstitutor);
            var type = (ClassType) getExpressionType(self.getExpression());
            var transformed = new UpdateObjectNode(
                    null,
                    node.getName(),
                    currentScope().getLastNode(),
                    currentScope()
            );
            transformed.setObjectId(self);
            for (UpdateField updateField : node.getUpdateFields()) {
                transformed.setUpdateField(
                        type.tryGetFieldByName(updateField.getField().getName()),
                        updateField.getOp(),
                        updateField.getValue().accept(valueSubstitutor)
                );
            }
            return transformed;
        }

        @Override
        public NodeRT<?> visitRaiseNode(RaiseNode node) {
            return new RaiseNode(
                    null, node.getName(),
                    node.getParameterKind(),
                    NncUtils.get(node.getException(), v -> v.accept(valueSubstitutor)),
                    NncUtils.get(node.getMessage(), v -> v.accept(valueSubstitutor)),
                    currentScope().getLastNode(),
                    currentScope()
            );
        }

        @Override
        public NodeRT<?> visitDeleteObjectNode(DeleteObjectNode node) {
            var transformed = new DeleteObjectNode(
                    null, node.getName(), currentScope().getLastNode(), currentScope()
            );
            transformed.setObjectId(node.getObjectId().accept(valueSubstitutor));
            return transformed;
        }

        @Override
        public NodeRT<?> visitMergeNode(MergeNode node) {
            var type = (ClassType) doSubstitution(node.getType());
            var branchNode = (BranchNode) NncUtils.requireNonNull(currentScope().getLastNode());
            var transformed = new MergeNode(
                    null, node.getName(),
                    branchNode,
                    type,
                    currentScope()
            );
            for (MergeNodeField field : node.getFields()) {
                var fieldCopy = new MergeNodeField(
                        type.tryGetFieldByName(field.getField().getName()),
                        transformed
                );
                for (ConditionalValue value : field.getValues()) {
                    var branch = branchNode.getBranchByIndex(value.getBranch().getIndex());
                    fieldCopy.setValue(branch, value.getValue().accept(valueSubstitutor));
                }
            }
            return transformed;
        }

        @Override
        public NodeRT<?> visitValueNode(ValueNode valueNode) {
            return new ValueNode(
                    null, valueNode.getName(),
                    substituteType(valueNode.getType()),
                    currentScope().getLastNode(),
                    currentScope(),
                    valueNode.getValue()
            );
        }

        @Override
        public NodeRT<?> visitGetUniqueNode(GetUniqueNode node) {
            var type = (ClassType) substituteType(node.getConstraint().getDeclaringType());
            var index = (Index) type.getConstraints().get(
                    node.getConstraint().getDeclaringType().getConstraints().indexOf(node.getConstraint())
            );
            return new GetUniqueNode(
                    null, node.getName(), index,
                    currentScope().getLastNode(), currentScope()
            );
        }

        @Override
        public NodeRT<?> visitUpdateStaticNode(UpdateStaticNode node) {
            var transformed = new UpdateStaticNode(
                    null, node.getName(), currentScope().getLastNode(), currentScope(),
                    node.getUpdateType()
            );
            for (UpdateField field : node.getFields()) {
                transformed.setUpdateField(field.getField(), field.getOp(),
                        field.getValue().accept(valueSubstitutor));
            }
            return transformed;
        }

    };

    private final SubstitutorBase<Argument> argumentTransformer = new SubstitutorBase<>() {

        @Override
        public Argument visitArgument(Argument argument) {
            return new Argument(
                    null,
                    currentCallable().getParameterByTemplate(argument.getParameter()),
                    argument.getValue().accept(valueSubstitutor)
            );
        }

    };

    public FieldData transformFieldData(FieldData fieldData) {
        return new FieldData(
                null,
                fieldData.getName(),
                fieldData.getCode(),
                fieldData.getColumn(),
                fieldData.isUnique(),
                fieldData.isAsTitle(),
                currentClass(),
                fieldData.getAccess(),
                substituteType(fieldData.getType()),
                fieldData.isChild(),
                fieldData.isStatic(),
                new NullInstance(typeFactory.getNullType()),
                new NullInstance(typeFactory.getNullType())
        );
    }

    private static class SubstitutorBase<T> extends ElementVisitor<T> {

        @Override
        public T visitElement(Element element) {
            throw new UnsupportedOperationException();
        }

    }

    private final ElementVisitor<Expression> expressionSubstitutor = new SubstitutorBase<>() {

        @Override
        public Expression visitExpression(Expression expression) {
            return expression.substituteChildren(
                    NncUtils.map(expression.getChildren(), c -> c.accept(this))
            );
        }

        @Override
        public Expression visitStaticFieldExpression(StaticFieldExpression expression) {
            var declaringType = (ClassType) substituteType(expression.getField().getDeclaringType());
            return new StaticFieldExpression(substituteField(declaringType, expression.getField()));
        }

        @Override
        public Expression visitPropertyExpression(PropertyExpression expression) {
            var instance = expression.getInstance().accept(this);
            return new PropertyExpression(
                    instance,
                    substituteProperty((ClassType) instance.getType(), expression.getProperty())
            );
        }
    };


}
