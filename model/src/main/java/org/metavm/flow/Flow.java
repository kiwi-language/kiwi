package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.expression.VoidStructuralVisitor;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@EntityType
public abstract class Flow extends AttributedElement implements GenericDeclaration, Callable, LoadAware, CapturedTypeScope, ITypeDef {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final IndexDef<Flow> IDX_HORIZONTAL_TEMPLATE =
            IndexDef.create(Flow.class, "horizontalTemplate");

    @EntityField(asTitle = true)
    private @NotNull String name;
    private boolean isNative;
    private boolean isSynthetic;
    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private @NotNull Type returnType;
    @ChildEntity
    private @Nullable Code code;
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
    private ReadWriteArray<Type> typeArguments = addEphemeralChild(new ReadWriteArray<>(Type.class), "typeArguments");
    private @NotNull MetadataState state;
    private @NotNull FunctionType type;
    @Nullable
    private final CodeSource codeSource;
    @ChildEntity
    private final ChildArray<CapturedTypeVariable> capturedTypeVariables = addChild(new ChildArray<>(CapturedTypeVariable.class), "capturedTypeVariables");
    @ChildEntity
    private final ChildArray<Lambda> lambdas = addChild(new ChildArray<>(Lambda.class), "lambdas");
    @ChildEntity
    @Nullable
    private ConstantPool constantPool;

    private transient ResolutionStage stage = ResolutionStage.INIT;
    private transient List<Node> nodes = new ArrayList<>();
    private transient Set<String> nodeNames = new HashSet<>();

    public Flow(Long tmpId,
                @NotNull String name,
                boolean isNative,
                boolean isSynthetic,
                List<Parameter> parameters,
                @NotNull Type returnType,
                List<TypeVariable> typeParameters,
                List<? extends Type> typeArguments,
                @Nullable Flow horizontalTemplate,
                @Nullable CodeSource codeSource,
                @NotNull MetadataState state
    ) {
        super(tmpId);
        if (horizontalTemplate == null && typeParameters.isEmpty() && !typeArguments.isEmpty())
            throw new InternalException("Missing flow template");
        this.name = NamingUtils.ensureValidName(name);
        this.isNative = isNative;
        this.isSynthetic = isSynthetic;
        this.returnType = returnType;
        this.type = new FunctionType(NncUtils.map(parameters, Parameter::getType), returnType);
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
    public @NotNull Code getCode() {
        return Objects.requireNonNull(code, () -> "Root scope not present in flow: " + getQualifiedName());
    }

    public boolean isConstantPoolPresent() {
        return constantPool != null;
    }

    public boolean isCodePresent() {
        return code != null;
    }

    @Override
    public void onLoadPrepare() {
         typeArguments = addEphemeralChild(new ReadWriteArray<>(Type.class), "typeArguments");
    }

    @Override
    public void onLoad() {
        if(!typeParameters.isEmpty())
            typeArguments.addAll(NncUtils.map(typeParameters, TypeVariable::getType));
        stage = ResolutionStage.INIT;
        nodeNames = new HashSet<>();
        accept(new VoidStructuralVisitor() {
            @Override
            public Void visitNode(Node node) {
                nodeNames.add(node.getName());
                return super.visitNode(node);
            }
        });
        if (codeSource != null) {
            codeSource.generateCode(this);
            computeMaxes();
            emitCode();
        }
    }

    public boolean matches(String name, List<Type> argumentTypes) {
        if (this.name.equals(name)) {
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

    public boolean isError() {
        return getState() == MetadataState.ERROR;
    }

    public void clearContent() {
        clearNodes();
        if(constantPool != null)
            constantPool.clear();
        capturedTypeVariables.clear();
    }

    public void clearNodes() {
        if (nodes != null)
            nodes.clear();
        if (code != null)
            code.clear();
        nodeNames.clear();
    }

    public boolean isSynthetic() {
        return isSynthetic;
    }

    void setSynthetic(boolean synthetic) {
        isSynthetic = synthetic;
    }

    private List<Node> nodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
            if (code != null) {
                code.accept(new VoidStructuralVisitor() {
                    @Override
                    public Void visitNode(Node node) {
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

    public @NotNull MetadataState getState() {
        return state;
    }

    public void setName(@NotNull String name) {
        this.name = name;
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
    public List<Node> getNodes() {
        return nodes();
    }

    void addNode(Node node) {
        nodeNames.add(node.getName());
        nodes().add(node);
        version++;
    }

    void removeNode(Node node) {
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

    public Parameter getParameterByName(String name) {
        return parameters.get(Parameter::getName, name);
    }

    @Override
    public FunctionType getFunctionType() {
        return getType();
    }

    @SuppressWarnings("unused")
    public Node getNodeByName(String nodeName) {
        return NncUtils.filterOneRequired(nodes(), n -> n.getName().equals(nodeName),
                "Node '" + nodeName + "' does not exist");
    }

    @SuppressWarnings("unused")
    public Node findNodeByName(String nodeName) {
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

    @Override
    public int getInputCount() {
        return parameters.size();
    }

    public @Nullable Parameter findParameter(Predicate<Parameter> predicate) {
        return NncUtils.find(parameters, predicate);
    }

    public Parameter getParameter(Predicate<Parameter> predicate) {
        return Objects.requireNonNull(findParameter(predicate),
                "Can not find parameter in flow " + this + " with predicate");
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
        resetBody();
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

    public String getSignatureString() {
        var name = isParameterized() ?
                getName() + "<" + NncUtils.join(getTypeArguments(), Type::getTypeDesc) + ">" : getName();
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
        if (isCodePresent())
            getCode().writeCode(writer);
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
        lambda.setFlow(this);
    }

    public void setLambdas(List<Lambda> lambdas) {
        this.lambdas.resetChildren(lambdas);
        lambdas.forEach(l -> l.setFlow(this));
    }

    public void forEachParameterized(Consumer<Flow> action) {
        ParameterizedStore.forEach(this, (k,v) -> action.accept((Flow) v));
    }

    public void resolveConstantPool() {
        if(constantPool != null)
            constantPool.resolve();
    }

    public ConstantPool getConstantPool() {
        return Objects.requireNonNull(constantPool,
                () -> "Constant pool is not initialized for flow " + getQualifiedName());
    }

    public void emitCode() {
        if(isCodePresent())
            getCode().emitCode();
        for (Lambda lambda : lambdas) {
            lambda.emitCode();
        }
    }

    protected void resetBody() {
        if(hasBody()) {
            code = addChild(new Code(this), "code");
            constantPool = addChild(new ConstantPool(), "constantPool");
        }
        else {
            code = null;
            constantPool = null;
        }
    }

    public boolean hasBody() {
        return !isNative;
    }

    public static final int FLAG_NATIVE = 1;
    public static final int FLAG_SYNTHETIC = 2;

    public int getFlags() {
        int flags = 0;
        if(isNative)
            flags |= FLAG_NATIVE;
        if(isSynthetic)
            flags |= FLAG_SYNTHETIC;
        return flags;
    }

    void setFlags(int flags) {
        isNative = (flags & FLAG_NATIVE) != 0;
        isSynthetic = (flags & FLAG_SYNTHETIC) != 0;
    }

    @Override
    public void write(KlassOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        output.writeInt(getFlags());
        output.writeInt(typeParameters.size());
        typeParameters.forEach(tp -> tp.write(output));
        output.writeInt(capturedTypeVariables.size());
        capturedTypeVariables.forEach(ctp -> ctp.write(output));
        output.writeInt(parameters.size());
        parameters.forEach(p -> p.write(output));
        returnType.write(output);
        output.write(state.code());
        if(constantPool != null)
            constantPool.write(output);
        if(code != null)
            code.write(output);
        output.writeInt(lambdas.size());
        lambdas.forEach(l -> l.write(output));
        writeAttributes(output);
    }

    @Override
    public void read(KlassInput input) {
        name = input.readUTF();
        setFlags(input.readInt());
        int typeParamCount = input.readInt();
        var typeParams = new ArrayList<TypeVariable>(typeParamCount);
        for (int i = 0; i < typeParamCount; i++) {
            typeParams.add(input.readTypeVariable());
        }
        setTypeParameters(typeParams);
        int capturedTypeVarCount = input.readInt();
        var capturedTypeVars = new ArrayList<CapturedTypeVariable>(capturedTypeVarCount);
        for (int i = 0; i < capturedTypeVarCount; i++) {
            capturedTypeVars.add(input.readCapturedTypeVariable());
        }
        setCapturedTypeVariables(capturedTypeVars);
        var paramCount = input.readInt();
        var params = new ArrayList<Parameter>(paramCount);
        for (int i = 0; i < paramCount; i++) {
            params.add(input.readParameter());
        }
        setParameters(params);
        returnType = input.readType();
        state = MetadataState.fromCode(input.read());
        if(hasBody()) {
            if(constantPool == null)
                constantPool = addChild(new ConstantPool(), "constantPool");
            if(code == null)
                code = addChild(new Code(this), "code");
        } else {
            constantPool = null;
            code = null;
        }
        if(constantPool != null) {
            constantPool.read(input);
        }
        if(code != null) {
            code.read(input);
        }
        var lambdaCount = input.readInt();
        var lambdas = new ArrayList<Lambda>();
        for (int i = 0; i < lambdaCount; i++) {
            lambdas.add(input.readLambda());
        }
        setLambdas(lambdas);
        readAttributes(input);
    }
}

