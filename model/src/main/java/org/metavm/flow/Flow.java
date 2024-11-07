package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.expression.VoidStructuralVisitor;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.FlowParam;
import org.metavm.flow.rest.FlowSignatureDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@EntityType
public abstract class Flow extends AttributedElement implements GenericDeclaration, Callable, LoadAware, CapturedTypeScope, ITypeDef {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final IndexDef<Flow> IDX_HORIZONTAL_TEMPLATE =
            IndexDef.create(Flow.class, "horizontalTemplate");

    @EntityField(asTitle = true)
    private @NotNull String name;
    @Nullable
    private String code;
    private boolean isNative;
    private final boolean isSynthetic;
    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private @NotNull Type returnType;
    @ChildEntity
    private @Nullable ScopeRT rootScope;
    private transient long version;
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isTemplate;
    @ChildEntity
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @CopyIgnore
    @Nullable
    private final Flow horizontalTemplate;
    @ChildEntity
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");
    private @NotNull MetadataState state;
    private @NotNull FunctionType type;
    @Nullable
    private final CodeSource codeSource;
    @ChildEntity
    private final ChildArray<CapturedTypeVariable> capturedTypeVariables = addChild(new ChildArray<>(CapturedTypeVariable.class), "capturedTypeVariables");
    @ChildEntity
    private final ChildArray<Lambda> lambdas = addChild(new ChildArray<>(Lambda.class), "lambdas");

    private transient ResolutionStage stage = ResolutionStage.INIT;
    private transient List<NodeRT> nodes = new ArrayList<>();
    private transient Set<String> nodeNames = new HashSet<>();

    public Flow(Long tmpId,
                @NotNull String name,
                @Nullable String code,
                boolean isNative,
                boolean isSynthetic,
                List<Parameter> parameters,
                @NotNull Type returnType,
                List<TypeVariable> typeParameters,
                List<? extends Type> typeArguments,
                @Nullable Flow horizontalTemplate,
                @Nullable CodeSource codeSource,
                @NotNull MetadataState state,
                boolean noCode
    ) {
        super(tmpId);
        if (horizontalTemplate == null && typeParameters.isEmpty() && !typeArguments.isEmpty())
            throw new InternalException("Missing flow template");
        this.name = NamingUtils.ensureValidName(name);
        this.code = NamingUtils.ensureValidFlowCode(code);
        this.isNative = isNative;
        this.isSynthetic = isSynthetic;
        this.returnType = returnType;
        this.type = new FunctionType(NncUtils.map(parameters, Parameter::getType), returnType);
        rootScope = !noCode && codeSource == null && !isNative ? addChild(new ScopeRT(this, this), "rootScope") : null;
        setTypeParameters(typeParameters);
        if(typeParameters.isEmpty())
            setTypeArguments(typeArguments);
        setParameters(parameters, false);
        this.horizontalTemplate = horizontalTemplate;
        this.codeSource = codeSource;
        this.state = state;
    }

    public abstract FlowExecResult execute(@Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext);

    public List<Type> getParameterTypes() {
        return NncUtils.map(parameters, Parameter::getType);
    }

    public @NotNull Type getReturnType() {
        return returnType;
    }

    @Override
    public @NotNull ScopeRT getScope() {
        return Objects.requireNonNull(rootScope, () -> "Root scope not present in flow: " + getQualifiedName());
    }

    public boolean isRootScopePresent() {
        return rootScope != null;
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
        nodeNames = new HashSet<>();
        accept(new VoidStructuralVisitor() {
            @Override
            public Void visitNode(NodeRT node) {
                nodeNames.add(node.getName());
                return super.visitNode(node);
            }
        });
        if (codeSource != null) {
            codeSource.generateCode(this);
            accept(new MaxesComputer());
        }
    }

    public boolean matches(String code, List<Type> argumentTypes) {
        if (this.code != null && this.code.equals(code)) {
            if (parameters.size() == argumentTypes.size()) {
                for (int i = 0; i < parameters.size(); i++) {
                    var paramType = parameters.get(i).getType();
                    var argType = argumentTypes.get(i);
                    if (!paramType.isConvertibleFrom(argType))
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    public FlowDTO toDTO(boolean includeCode, SerializeContext serContext) {
        if (includeCode)
            getTypeParameters().forEach(serContext::writeTypeDef);
        capturedTypeVariables.forEach(serContext::writeTypeDef);
        return new FlowDTO(
                serContext.getStringId(this),
                getName(),
                getCode(),
                isNative,
                isSynthetic,
                includeCode && isRootScopePresent() ? getScope().toDTO(true, serContext) : null,
                returnType.toExpression(serContext),
                NncUtils.map(parameters, Parameter::toDTO),
                type.toExpression(serContext),
                NncUtils.map(typeParameters, serContext::getStringId),
                NncUtils.get(horizontalTemplate, serContext::getStringId),
                NncUtils.map(typeArguments, t -> t.toExpression(serContext)),
                NncUtils.map(capturedTypeVariables, serContext::getStringId),
                List.of(),
                List.of(),
                NncUtils.map(lambdas, l -> l.toDTO(serContext)),
                isTemplate(),
                getAttributesMap(),
                getState().code(),
                getParam(includeCode, serContext)
        );
    }

    protected abstract FlowParam getParam(boolean includeCode, SerializeContext serializeContext);

    public boolean isError() {
        return getState() == MetadataState.ERROR;
    }

    public void clearContent() {
        clearNodes();
//        capturedFlows.clear();
        capturedTypeVariables.clear();
//        capturedCompositeTypes.clear();
    }

    public void clearNodes() {
        if (nodes != null)
            nodes.clear();
        if (rootScope != null)
            rootScope.clearNodes();
        nodeNames.clear();
    }

    public boolean isSynthetic() {
        return isSynthetic;
    }

    private List<NodeRT> nodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
            if (rootScope != null) {
                rootScope.accept(new VoidStructuralVisitor() {
                    @Override
                    public Void visitNode(NodeRT node) {
                        nodes.add(node);
                        return super.visitNode(node);
                    }
                });
            }
        }
        return nodes;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public String getQualifiedName() {
        return getNameWithTypeArguments();
    }

    public String getNameWithTypeArguments() {
        if (NncUtils.allMatch(typeArguments, t -> t instanceof VariableType v && v.getVariable().getGenericDeclaration() == this))
            return name;
        else
            return name + "<" + NncUtils.join(typeArguments, Type::getTypeDesc) + ">";
    }

    @Override
    @Nullable
    public String getCode() {
        return code;
    }

    public @NotNull MetadataState getState() {
        return state;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public boolean check() {
        return accept(new FlowChecker());
    }

    public void analyze() {
        accept(new FlowAnalyzer());
    }

    public void computeMaxes() {
        accept(new MaxesComputer());
    }

    @SuppressWarnings("unused")
    public List<NodeRT> getNodes() {
        return nodes();
    }

    void addNode(NodeRT node) {
        nodeNames.add(node.getName());
        nodes().add(node);
        version++;
    }

    void removeNode(NodeRT node) {
        nodeNames.remove(node.getName());
        nodes().remove(node);
        version++;
    }

    public @Nullable Flow getTemplate() {
        return horizontalTemplate;
    }

    public boolean getParameterizedFlows() {
        return horizontalTemplate != null;
    }

    public Parameter getParameterByCode(String code) {
        return parameters.get(Parameter::getCode, code);
    }

    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    @Override
    public FunctionType getFunctionType() {
        return getType();
    }

    public NodeRT getRootNode() {
        return getScope().tryGetFirstNode();
    }

    public ScopeRT newEphemeralRootScope() {
        NncUtils.requireTrue(codeSource != null);
        return rootScope = addChild(new ScopeRT(this, this, true), "rootScope");
    }

    @SuppressWarnings("unused")
    public NodeRT getNodeByName(String nodeName) {
        return NncUtils.filterOneRequired(nodes(), n -> n.getName().equals(nodeName),
                "Node '" + nodeName + "' does not exist");
    }

    @SuppressWarnings("unused")
    public NodeRT findNodeByName(String nodeName) {
        return NncUtils.find(nodes(), n -> n.getName().equals(nodeName));
    }

    public long getVersion() {
        return version;
    }

    public boolean isNative() {
        return isNative;
    }

    public List<TypeVariable> getTypeParameters() {
        return NncUtils.listOf(typeParameters);
    }

    public List<? extends Type> getEffectiveTypeArguments() {
        return horizontalTemplate == null ? NncUtils.map(typeParameters, TypeVariable::getType) : typeArguments.toList();
    }

    public void setParameters(List<Parameter> parameters) {
        setParameters(parameters, true);
    }

    private void setParameters(List<Parameter> parameters, boolean resetType) {
        parameters.forEach(p -> p.setCallable(this));
        this.parameters.resetChildren(parameters);
        if (resetType)
            resetType();
    }

    public void addParameter(Parameter parameter) {
        parameters.addChild(parameter);
        resetType();
    }

    protected void resetType() {
        type = new FunctionType(NncUtils.map(parameters, Parameter::getType), returnType);
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
        typeArguments.add(typeParameter.getType());
    }

    public List<Parameter> getParameters() {
        return parameters.toList();
    }

    public @Nullable Parameter findParameter(Predicate<Parameter> predicate) {
        return NncUtils.find(parameters, predicate);
    }

    public Parameter getParameter(Predicate<Parameter> predicate) {
        return Objects.requireNonNull(findParameter(predicate),
                "Can not find parameter in flow " + this + " with predicate");
    }


    public String getCodeNotNull() {
        return Objects.requireNonNull(code);
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
        resetType();
    }

    public @Nullable Flow getHorizontalTemplate() {
        return horizontalTemplate;
    }

    public Flow getEffectiveHorizontalTemplate() {
        return horizontalTemplate == null ? this : horizontalTemplate;
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    public boolean isParameterized() {
        return horizontalTemplate != null && horizontalTemplate != this;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        if (isTemplate() && !NncUtils.iterableEquals(NncUtils.map(this.typeParameters, TypeVariable::getType), typeArguments))
            throw new InternalException("Type arguments must equal to type parameters for a template flow. Actual type arguments: " + typeArguments);
        this.typeArguments.reset(typeArguments);
    }

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> {
            if (tp.getGenericDeclaration() != this)
                tp.setGenericDeclaration(this);
        });
        this.typeParameters.resetChildren(typeParameters);
        if (isTemplate())
            setTypeArguments(NncUtils.map(typeParameters, TypeVariable::getType));
    }

    public void setCapturedTypeVariables(List<CapturedTypeVariable> capturedTypeVariables) {
        capturedTypeVariables.forEach(ct -> {
            if (ct.getScope() != this)
                ct.setScope(this);
        });
        this.capturedTypeVariables.resetChildren(capturedTypeVariables);
    }

    public void setCapturedCompositeTypes(List<Type> capturedCompositeTypes) {
//        this.capturedCompositeTypes.resetChildren(capturedCompositeTypes);
    }

    public void setCapturedFlows(List<Flow> capturedFlows) {
//        this.capturedFlows.resetChildren(capturedFlows);
    }

    public @NotNull FunctionType getType() {
        return type;
    }

    public FlowSignatureDTO getSignature() {
        return getSignature(getName(), getParameterTypes());
    }

    public static FlowSignatureDTO getSignature(String name, List<Type> parameterTypes) {
        return new FlowSignatureDTO(name,
                NncUtils.map(parameterTypes, Entity::getStringId)
        );
    }

    public String getSignatureString() {
        var name = isParameterized() ?
                getCode() + "<" + NncUtils.join(getTypeArguments(), Type::getTypeDesc) + ">" : getCode();
        return name + "(" + NncUtils.join(getParameterTypes(), Type::getTypeDesc) + ")";
    }

    public void setState(@NotNull MetadataState state) {
        this.state = state;
    }

    protected List<Value> checkArguments(List<? extends Value> arguments) {
        List<Value> convertedArgs = new ArrayList<>();
        out:
        if (arguments.size() == parameters.size()) {
            var paramIt = parameters.iterator();
            var argIt = arguments.iterator();
            while (paramIt.hasNext() && argIt.hasNext()) {
                var param = paramIt.next();
                var arg = argIt.next();
                if (param.getType().isInstance(arg)) {
                    convertedArgs.add(arg);
                } else {
                    try {
                        convertedArgs.add(arg.convert(param.getType()));
                    } catch (BusinessException e) {
//                        if (DebugEnv.debugging) {
                            logger.info("Argument type mismatch: {} is not assignable from {}",
                                    param.getType().getTypeDesc(),
                                    arg.getType().getTypeDesc());
//                        }
                        break out;
                    }
                }
            }
            return convertedArgs;
        }
        logger.info("class: {}, number type parameters: {}", EntityUtils.getRealType(this).getSimpleName(), getTypeParameters().size());
        throw new BusinessException(ErrorCode.ILLEGAL_FUNCTION_ARGUMENT, getQualifiedName(),
                NncUtils.join(getParameterTypes(), Type::getTypeDesc),
                NncUtils.join(arguments, arg -> arg.getType().getTypeDesc()));
    }

    @Override
    public Collection<CapturedTypeVariable> getCapturedTypeVariables() {
        return capturedTypeVariables.toList();
    }

    public Collection<Type> getCapturedCompositeTypes() {
        return List.of();
//        return capturedCompositeTypes.toList();
    }

    public Collection<Flow> getCapturedFlows() {
        return List.of();
//        return capturedFlows.toList();
    }

    @Override
    public int getCapturedTypeVariableIndex(CapturedTypeVariable capturedTypeVariable) {
        int index = 0;
        for (var type : capturedTypeVariables) {
            if (type == capturedTypeVariable)
                return index;
            if (type.getUncertainType() == capturedTypeVariable.getUncertainType())
                index++;
        }
        throw new InternalException("Captured type not found: " + capturedTypeVariable);
    }

    public void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable) {
        if (capturedTypeVariables.contains(capturedTypeVariable))
            throw new InternalException("Captured type already present: " + EntityUtils.getEntityDesc(capturedTypeVariable));
        capturedTypeVariables.addChild(capturedTypeVariable);
    }

    public void addCapturedCompositeType(Type compositeType) {
//        capturedCompositeTypes.addChild(compositeType);
    }

    public void addCapturedFlow(Flow flow) {
//        capturedFlows.addChild(flow);
    }

    public ResolutionStage getStage() {
        return stage();
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var curStage = this.stage;
        this.stage = stage;
        return curStage;
    }

    public void writeCode(CodeWriter writer) {
        writer.writeNewLine(
                "Flow "
                        + name
                        + " (" + NncUtils.join(parameters, Parameter::getText, ", ")
                        + ")"
                        + ": " + getReturnType().getName()
        );
        if (isRootScopePresent())
            getScope().writeCode(writer);
        else
            writer.write(";");
    }

    public String getText() {
        CodeWriter writer = new CodeWriter();
        writeCode(writer);
        return writer.toString();
    }

    @Override
    public String getScopeName() {
        return getNameWithTypeArguments();
    }

    @Override
    public String getTypeDesc() {
        return getNameWithTypeArguments();
    }

    public void addParameterized(Flow parameterized) {
        NncUtils.requireTrue(parameterized.getTemplate() == this);
        NncUtils.requireNull(ParameterizedStore.put(this, parameterized.typeArguments.secretlyGetTable(), parameterized),
                () -> "Parameterized flow " + parameterized.getTypeDesc() + " already exists");
    }

    protected abstract Flow createParameterized(List<? extends Type> typeArguments);

    public @Nullable Flow getExistingParameterized(List<? extends Type> typeArguments) {
        if (typeArguments.equals(NncUtils.map(typeParameters, TypeVariable::getType)))
            return this;
        return (Flow) ParameterizedStore.get(this,typeArguments);
    }

    private ResolutionStage stage() {
        if(stage == null)
            stage = ResolutionStage.INIT;
        return stage;
    }

    public Flow getParameterized(List<? extends Type> typeArguments) {
        var pFlow = getExistingParameterized(typeArguments);
        if(pFlow == this)
            return pFlow;
        if(pFlow == null) {
            pFlow = createParameterized(typeArguments);
            addParameterized(pFlow);
        }
        else if (pFlow.getStage().isAfterOrAt(stage()))
            return pFlow;
        var subst = new SubstitutorV2(this, typeParameters.toList(), typeArguments, pFlow, ResolutionStage.DEFINITION);
        return substitute(subst);
    }

    protected Flow substitute(SubstitutorV2 substitutor) {
        return (Flow) substitutor.copy(this);
    }

    public abstract FlowRef getRef();

    public String nextNodeName(String prefix) {
        if(!nodeNames.contains(prefix))
            return prefix;
        int n = 1;
        while (nodeNames.contains(prefix + "_" + n))
            n++;
        return prefix + "_" + n;
    }

    public List<Lambda> getLambdas() {
        return lambdas.toList();
    }

    public void addLambda(Lambda lambda) {
        this.lambdas.addChild(lambda);
    }

    public void setLambdas(List<Lambda> lambdas) {
        this.lambdas.resetChildren(lambdas);
    }

}

