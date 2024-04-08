package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.expression.VoidStructuralVisitor;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowParam;
import tech.metavm.flow.rest.FlowSignatureDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.FlowInfo;
import tech.metavm.object.type.rest.dto.ParameterizedFlowDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@EntityType("流程")
public abstract class Flow extends Element implements GenericDeclaration, Callable, LoadAware, CapturedTypeScope {

    public static final IndexDef<Flow> IDX_PARAMETERIZED_KEY =
            IndexDef.createUnique(Flow.class, "parameterizedKey");

    public static final IndexDef<Flow> IDX_HORIZONTAL_TEMPLATE =
            IndexDef.create(Flow.class, "horizontalTemplate");

    @EntityField(value = "名称", asTitle = true)
    private @NotNull String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("是否原生")
    private boolean isNative;
    @EntityField("是否合成")
    private final boolean isSynthetic;
    @ChildEntity("参数列表")
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    @EntityField("返回类型")
    private @NotNull Type returnType;
    @ChildEntity("根流程范围")
    private @Nullable ScopeRT rootScope;
    private transient long version;
    // Don't remove, for search
    @SuppressWarnings("unused")
    @EntityField("是否模版")
    private boolean isTemplate;
    @ChildEntity("类型参数")
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @EntityField("水平模板")
    @CopyIgnore
    @Nullable
    private final Flow horizontalTemplate;
    @ChildEntity("类型实参列表")
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");
    @EntityField("状态")
    private @NotNull MetadataState state;
    @EntityField("类型")
    private @NotNull FunctionType type;
    @EntityField("参数化键")
    @Nullable
    @CopyIgnore
    private String parameterizedKey;
    @Nullable
    @EntityField("代码来源")
    private final CodeSource codeSource;
    @ChildEntity("模板实例列表")
    @CopyIgnore
    private @Nullable ChildArray<Flow> templateInstances;
    @ChildEntity("捕获类型列表")
    private final ChildArray<CapturedType> capturedTypes = addChild(new ChildArray<>(CapturedType.class), "capturedTypes");

    private transient ResolutionStage stage = ResolutionStage.INIT;
    private transient ReadWriteArray<ScopeRT> scopes = new ReadWriteArray<>(ScopeRT.class);
    private transient ReadWriteArray<NodeRT> nodes = new ReadWriteArray<>(NodeRT.class);

    public Flow(Long tmpId,
                @NotNull String name,
                @Nullable String code,
                boolean isNative,
                boolean isSynthetic,
                List<Parameter> parameters,
                @NotNull Type returnType,
                List<TypeVariable> typeParameters,
                List<Type> typeArguments,
                @NotNull FunctionType type,
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
        this.type = type;
        rootScope = !noCode && codeSource == null && !isNative ? addChild(new ScopeRT(this), "rootScope") : null;
        setTypeParameters(typeParameters);
        setTypeArguments(typeArguments);
        setParameters(parameters, false);
        this.horizontalTemplate = horizontalTemplate;
        this.codeSource = codeSource;
        this.state = state;
    }

    public abstract FlowExecResult execute(@Nullable ClassInstance self, List<Instance> arguments, CallContext callContext);

    public List<Type> getParameterTypes() {
        return NncUtils.map(parameters, Parameter::getType);
    }

    public @NotNull Type getReturnType() {
        return returnType;
    }

    public @NotNull ScopeRT getRootScope() {
        return Objects.requireNonNull(rootScope);
    }

    public boolean isRootScopePresent() {
        return rootScope != null;
    }

    public ScopeRT getScope(long id) {
        return scopes().get(Entity::tryGetId, id);
    }

    @Override
    public boolean afterContextInitIds() {
        if (horizontalTemplate != null || isTemplate()) {
            if (parameterizedKey == null)
                parameterizedKey = Types.getParameterizedKey(getEffectiveHorizontalTemplate(), typeArguments);
        }
        return true;
    }

    @Override
    public void onLoad(IEntityContext context) {
        stage = ResolutionStage.INIT;
        if (codeSource != null)
            codeSource.generateCode(this, CompositeTypeFacadeImpl.fromContext(context));
    }

    @SuppressWarnings("unused")
    public ReadWriteArray<ScopeRT> getScopes() {
        return scopes();
    }

    @SuppressWarnings("unused")
    public void addScope(ScopeRT scope) {
        this.scopes().add(scope);
    }

    public FlowDTO toDTO(boolean includeCode, SerializeContext serContext) {
        if (includeCode) {
            getTypeParameters().forEach(serContext::writeType);
            serContext.writeType(getType());
            serContext.writeType(returnType);
        }
        capturedTypes.forEach(serContext::writeType);
        return new FlowDTO(
                serContext.getId(this),
                getName(),
                getCode(),
                isNative,
                includeCode && isRootScopePresent() ? getRootScope().toDTO(true, serContext) : null,
                serContext.getId(getReturnType()),
                NncUtils.map(parameters, Parameter::toDTO),
                serContext.getId(getType()),
                NncUtils.map(typeParameters, serContext::getId),
                NncUtils.get(horizontalTemplate, serContext::getId),
                NncUtils.map(typeArguments, serContext::getId),
                NncUtils.map(capturedTypes, serContext::getId),
List.of(),
List.of(),
//                NncUtils.map(capturedCompositeTypes, serContext::getId),
//                NncUtils.map(capturedFlows, serContext::getId),
                isTemplate(),
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
        capturedTypes.clear();
//        capturedCompositeTypes.clear();
    }

    public void clearNodes() {
        if (nodes != null)
            nodes.clear();
        if (rootScope != null)
            rootScope.clearNodes();
    }

    public boolean isSynthetic() {
        return isSynthetic;
    }

    public InputNode getInputNode() {
        return (InputNode) NncUtils.findRequired(getRootScope().getNodes(), node -> node instanceof InputNode);
    }

    private ReadWriteArray<NodeRT> nodes() {
        if (nodes == null) {
            nodes = new ReadWriteArray<>(new TypeReference<>() {
            });
            if(rootScope != null) {
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
        if(NncUtils.allMatch(typeArguments, t -> t instanceof TypeVariable tv && tv.getGenericDeclaration() == this))
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

    public void addTemplateInstance(Flow flow) {
        assert !typeParameters.isEmpty();
        if (templateInstances == null)
            templateInstances = addChild(new ChildArray<>(Flow.class), "templateInstances");
        templateInstances.addChild(flow);
    }

    public boolean check() {
        return accept(new FlowChecker());
    }

    public void analyze() {
        accept(new FlowAnalyzer());
    }

    private ReadWriteArray<ScopeRT> scopes() {
        if (scopes == null)
            scopes = new ReadWriteArray<>(ScopeRT.class);
        return scopes;
    }

    public NodeRT getNode(long id) {
        return nodes().get(Entity::tryGetId, id);
    }

    @SuppressWarnings("unused")
    public ReadonlyArray<NodeRT> getNodes() {
        return nodes();
    }

    void addNode(NodeRT node) {
        nodes().add(node);
        version++;
    }

    void removeNode(NodeRT node) {
        nodes().remove(node);
        version++;
    }

    public @Nullable Flow getTemplate() {
        return horizontalTemplate;
    }

    public boolean isParameterized() {
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

    @Override
    public void setFunctionType(FunctionType functionType) {
        setType(functionType);
    }

    public NodeRT getRootNode() {
        return getRootScope().tryGetFirstNode();
    }

    public ScopeRT newEphemeralRootScope() {
        NncUtils.requireTrue(codeSource != null);
        return rootScope = addChild(new ScopeRT(this, null, false, true), "rootScope");
    }

    @SuppressWarnings("unused")
    public NodeRT getNodeByNameRequired(String nodeName) {
        return NncUtils.filterOneRequired(nodes(), n -> n.getName().equals(nodeName),
                "流程节点'" + nodeName + "'不存在");
    }

    @SuppressWarnings("unused")
    public NodeRT getNodeByName(String nodeName) {
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

    public ReadonlyArray<? extends Type> getEffectiveTypeArguments() {
        return horizontalTemplate == null ? typeParameters : typeArguments;
    }

    public void setParameters(List<Parameter> parameters) {
        setParameters(parameters, true);
    }

    private void setParameters(List<Parameter> parameters, boolean check) {
        if (check)
            checkTypes(parameters, returnType, type);
        parameters.forEach(p -> p.setCallable(this));
        this.parameters.resetChildren(parameters);
    }

    protected void checkTypes(List<Parameter> parameters, Type returnType, FunctionType type) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        if (!type.getParameterTypes().equals(paramTypes) || !type.getReturnType().equals(returnType))
            throw new InternalException("Incorrect function type: " + type);
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
        typeArguments.add(typeParameter);
    }

    public List<Parameter> getParameters() {
        return NncUtils.listOf(parameters);
    }

    public String getCodeRequired() {
        return Objects.requireNonNull(code);
    }

    public void setReturnType(Type returnType) {
        checkTypes(parameters.toList(), returnType, type);
        this.returnType = returnType;
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

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public void update(List<Parameter> parameters, Type returnType, FunctionTypeProvider functionTypeProvider) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        var type = functionTypeProvider.getFunctionType(paramTypes, returnType);
        checkTypes(parameters, returnType, type);
        updateInternal(parameters, returnType, type);
    }

    protected void updateInternal(List<Parameter> parameters, Type returnType, FunctionType type) {
        setParameters(parameters, false);
        this.type = type;
        this.returnType = returnType;
    }

    public void setType(Type type) {
        if (type instanceof FunctionType functionType) {
            checkTypes(parameters.toList(), returnType, functionType);
            this.type = functionType;
        } else
            throw new InternalException("Not a function type");
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        if (isTemplate() && !NncUtils.iterableEquals(this.typeParameters, typeArguments))
            throw new InternalException("Type arguments must equal to type parameters for a template flow");
        this.typeArguments.reset(typeArguments);
        this.parameterizedKey = null;
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
            setTypeArguments(typeParameters);
    }

    public void setCapturedTypes(List<CapturedType> capturedTypes) {
        capturedTypes.forEach(ct -> {
            if(ct.getScope() != this)
                ct.setScope(this);
        });
        this.capturedTypes.resetChildren(capturedTypes);
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
        return getCode() + "(" + NncUtils.join(getParameterTypes(), Type::getCode) + ")";
    }

    public FlowInfo toGenericElementDTO(SerializeContext serializeContext) {
        return new FlowInfo(
                serializeContext.getId(Objects.requireNonNull(getTemplate())),
                serializeContext.getId(this),
                NncUtils.map(parameters, p -> p.toGenericElementDTO(serializeContext)),
                NncUtils.map(typeParameters, tp -> tp.toGenericElementDTO(serializeContext))
        );
    }

    public ParameterizedFlowDTO toPFlowDTO(SerializeContext serializeContext) {
        return new ParameterizedFlowDTO(
                serializeContext.getId(Objects.requireNonNull(getHorizontalTemplate())),
                serializeContext.getId(this),
                NncUtils.map(typeArguments, serializeContext::getId),
                NncUtils.map(parameters, p -> p.toGenericElementDTO(serializeContext))
        );
    }

    public void setState(@NotNull MetadataState state) {
        this.state = state;
    }

    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("Debug");

    protected void checkArguments(List<Instance> arguments) {
        out:
        if (arguments.size() == parameters.size()) {
            var paramIt = parameters.iterator();
            var argIt = arguments.iterator();
            while (paramIt.hasNext() && argIt.hasNext()) {
                var param = paramIt.next();
                var arg = argIt.next();
                if (!param.getType().isInstance(arg)) {
                    if(DebugEnv.DEBUG_LOG_ON) {
                        DEBUG_LOGGER.info("Argument type mismatch: {} is not assignable from {}",
                                param.getType().getTypeDesc(),
                                arg.getType().getTypeDesc());
                    }
                    break out;
                }
            }
            return;
        }
        throw new BusinessException(ErrorCode.ILLEGAL_FUNCTION_ARGUMENT, getTypeDesc(),
                NncUtils.join(getParameterTypes(), Type::getTypeDesc),
                NncUtils.join(arguments, arg -> EntityUtils.getEntityDesc(arg.getType())));
    }

    @Override
    public Collection<CapturedType> getCapturedTypes() {
        return capturedTypes.toList();
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
    public int getCapturedTypeIndex(CapturedType capturedType) {
        int index = 0;
        for (var type : capturedTypes) {
            if (type == capturedType)
                return index;
            if(type.getUncertainType() == capturedType.getUncertainType())
                index++;
        }
        throw new InternalException("Captured type not found: " + capturedType);
    }

    public void addCapturedType(CapturedType capturedType) {
        if(capturedTypes.contains(capturedType))
            throw new InternalException("Captured type already present: " + EntityUtils.getEntityDesc(capturedType));
        capturedTypes.addChild(capturedType);
    }

    public void addCapturedCompositeType(Type compositeType) {
//        capturedCompositeTypes.addChild(compositeType);
    }

    public void addCapturedFlow(Flow flow) {
//        capturedFlows.addChild(flow);
    }

    public ResolutionStage getStage() {
        return stage;
    }

    public void setStage(ResolutionStage stage) {
        this.stage = stage;
    }

    public void writeCode(CodeWriter writer) {
        writer.writeNewLine(
                "Flow "
                        + name
                        + " (" + NncUtils.join(parameters, Parameter::getText, ", ")
                        + ")"
                        + ": " + getReturnType().getName()
        );
        if(isRootScopePresent())
            getRootScope().writeCode(writer);
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

}

