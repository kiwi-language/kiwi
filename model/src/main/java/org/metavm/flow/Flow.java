package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
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

@Entity
public abstract class Flow extends AttributedElement implements GenericDeclaration, Callable, LoadAware, CapturedTypeScope, ITypeDef {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @EntityField(asTitle = true)
    private @NotNull String name;
    private boolean isNative;
    private boolean isSynthetic;
    @ChildEntity
    private final ChildArray<Parameter> parameters = addChild(new ChildArray<>(Parameter.class), "parameters");
    private int returnTypeIndex;
    @ChildEntity
    private @Nullable Code code;
    private transient long version;
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isTemplate;
    @ChildEntity
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    private @NotNull MetadataState state;
    private int typeIndex;
    @Nullable
    private final CodeSource codeSource;
    @ChildEntity
    private final ChildArray<CapturedTypeVariable> capturedTypeVariables = addChild(new ChildArray<>(CapturedTypeVariable.class), "capturedTypeVariables");
    @ChildEntity
    private final ChildArray<Lambda> lambdas = addChild(new ChildArray<>(Lambda.class), "lambdas");
    @ChildEntity
    private final ChildArray<Klass> klasses = addChild(new ChildArray<>(Klass.class), "klasses");
    @ChildEntity
    private final ConstantPool constantPool = addChild(new ConstantPool(), "constantPool");

    private transient ResolutionStage stage = ResolutionStage.INIT;
    private transient List<Node> nodes = new ArrayList<>();
    private transient Set<String> nodeNames = new HashSet<>();

    public Flow(Long tmpId,
                @NotNull String name,
                boolean isNative,
                boolean isSynthetic,
                List<NameAndType> parameters,
                @NotNull Type returnType,
                List<TypeVariable> typeParameters,
                @Nullable CodeSource codeSource,
                @NotNull MetadataState state
    ) {
        super(tmpId);
        this.name = NamingUtils.ensureValidName(name);
        this.isNative = isNative;
        this.isSynthetic = isSynthetic;
        this.returnTypeIndex = constantPool.addValue(returnType);
        this.typeIndex = constantPool.addValue(new FunctionType(NncUtils.map(parameters, NameAndType::type), returnType));
        setTypeParameters(typeParameters);
        setParameters(NncUtils.map(parameters, p -> new Parameter(null, p.name(), p.type(), this)), false);
        this.codeSource = codeSource;
        this.state = state;
    }

    public abstract FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext);

    public List<Type> getParameterTypes(TypeMetadata typeMetadata) {
        return NncUtils.map(parameters, p -> p.getType(typeMetadata));
    }

    public List<Type> getParameterTypes() {
        return getParameterTypes(getConstantPool());
    }

    public @NotNull Type getReturnType(TypeMetadata typeMetadata) {
        if(typeMetadata.getType(returnTypeIndex) == null)
            throw new NullPointerException("Return type is missing for flow " + getQualifiedName());
        return typeMetadata.getType(returnTypeIndex);
    }

    public Type getReturnType() {
        return getReturnType(constantPool);
    }

    @Override
    public @NotNull Code getCode() {
        return Objects.requireNonNull(code, () -> "Root scope not present in flow: " + getQualifiedName());
    }

    public boolean isCodePresent() {
        return code != null;
    }

    @Override
    public void onLoad() {
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

    public boolean isError() {
        return getState() == MetadataState.ERROR;
    }

    public void clearContent() {
        clearNodes();
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
        return getName();
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
        typeIndex = constantPool.addValue(new FunctionType(NncUtils.map(parameters, Parameter::getType), constantPool.getType(returnTypeIndex)));
    }

    @Override
    public int getTypeIndex() {
        return typeIndex;
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
    }

    public List<Parameter> getParameters() {
        return parameters.toList();
    }

    @Override
    public int getInputCount() {
        return parameters.size();
    }

    public int getTypeInputCount() {
        return typeParameters.size();
    }

    public @Nullable Parameter findParameter(Predicate<Parameter> predicate) {
        return NncUtils.find(parameters, predicate);
    }

    public Parameter getParameter(Predicate<Parameter> predicate) {
        return Objects.requireNonNull(findParameter(predicate),
                "Can not find parameter in flow " + this + " with predicate");
    }

    public void setReturnType(Type returnType) {
        this.returnTypeIndex = constantPool.addValue(returnType);
        resetType();
    }

    public List<Type> getDefaultTypeArguments() {
        return NncUtils.map(typeParameters, TypeVariable::getType);
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
        resetBody();
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
    }

    public void setCapturedTypeVariables(List<CapturedTypeVariable> capturedTypeVariables) {
        capturedTypeVariables.forEach(ct -> {
            if (ct.getScope() != this)
                ct.setScope(this);
        });
        this.capturedTypeVariables.resetChildren(capturedTypeVariables);
    }

    public @NotNull FunctionType getType() {
        return constantPool.getFunctionType(typeIndex);
    }

    public FunctionType getType(TypeMetadata typeMetadata) {
        return typeMetadata.getFunctionType(typeIndex);
    }

    public String getSignatureString() {
        return name + "(" + NncUtils.join(getParameterTypes(), Type::getTypeDesc) + ")";
    }

    public void setState(@NotNull MetadataState state) {
        this.state = state;
    }

    protected void checkArguments(List<? extends Value> arguments, TypeMetadata typeMetadata) {
        if (arguments.size() != parameters.size())
            throw new BusinessException(ErrorCode.INCORRECT_ARGUMENT_COUNT, this, parameters.size(), arguments.size());
        var argIt = arguments.iterator();
        for (Parameter param : parameters) {
            var arg = argIt.next();
            var paramType = param.getType(typeMetadata);
            if (!paramType.isInstance(paramType.getUnderlyingType().fromStackValue(arg)))
                throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT2, arg, this, param.getName(), param.getType());
        }
    }

    @Override
    public Collection<CapturedTypeVariable> getCapturedTypeVariables() {
        return capturedTypeVariables.toList();
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
        return name;
    }

    @Override
    public String getTypeDesc() {
        return name;
    }

    private ResolutionStage stage() {
        if(stage == null)
            stage = ResolutionStage.INIT;
        return stage;
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
        if(hasBody())
            code = addChild(new Code(this), "code");
        else
            code = null;
    }

    public void addLocalKlass(Klass localKlass) {
        klasses.addChild(localKlass);
        localKlass.setEnclosingFlow(this);
    }

    public List<Klass> getKlasses() {
        return klasses.toList();
    }

    public void setKlasses(List<Klass> klasses) {
        this.klasses.resetChildren(klasses);
        klasses.forEach(k -> k.setEnclosingFlow(this));
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
    public void write(MvOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        output.writeInt(getFlags());
        constantPool.write(output);
        output.writeInt(typeParameters.size());
        typeParameters.forEach(tp -> tp.write(output));
        output.writeInt(capturedTypeVariables.size());
        capturedTypeVariables.forEach(ctp -> ctp.write(output));
        output.writeInt(parameters.size());
        parameters.forEach(p -> p.write(output));
        output.writeShort(returnTypeIndex);
        output.writeShort(typeIndex);
        output.write(state.code());
        if(code != null)
            code.write(output);
        output.writeInt(lambdas.size());
        lambdas.forEach(l -> l.write(output));
        writeAttributes(output);
        output.writeInt(klasses.size());
        klasses.forEach(k -> k.write(output));
    }

    @Override
    public void read(KlassInput input) {
        name = input.readUTF();
        setFlags(input.readInt());
        constantPool.read(input);
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
        parameters.clear();
        for (int i = 0; i < paramCount; i++) {
            parameters.addChild(input.readParameter());
        }
        returnTypeIndex = input.readShort();
        typeIndex = input.readShort();
        state = MetadataState.fromCode(input.read());
        if(hasBody()) {
            if(code == null)
                code = addChild(new Code(this), "code");
        } else {
            code = null;
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
        var localKlassCount = input.readInt();
        klasses.clear();
        for (int i = 0; i < localKlassCount; i++) {
            klasses.addChild(input.readKlass());
        }
    }

    public ConstantPool getExistingTypeMetadata(List<? extends Type> typeArguments) {
        if (NncUtils.map(getAllTypeParameters(), TypeVariable::getType).equals(typeArguments))
            return constantPool;
        return (ConstantPool) ParameterizedStore.get(this, typeArguments);
    }

    private ConstantPool createTypeMetadata(List<? extends Type> typeArguments) {
        return new ConstantPool(typeArguments);
    }

    public void addTypeMetadata(ConstantPool parameterized) {
        var existing = ParameterizedStore.get(this, parameterized.typeArguments.secretlyGetTable());
        if(existing != null)
            throw new IllegalStateException("Parameterized klass " + parameterized + " already exists. "
                    + "existing: " + System.identityHashCode(existing) + ", new: "+ System.identityHashCode(parameterized)
            );
        NncUtils.requireNull(ParameterizedStore.put(this, parameterized.typeArguments.secretlyGetTable(), parameterized),
                () -> "Parameterized klass " + parameterized + " already exists");
    }

    public boolean isConstantPoolParameterized() {
        return isTemplate;
    }

    public TypeMetadata getTypeMetadata(List<? extends Type> typeArguments) {
        if (!isConstantPoolParameterized()) {
            if (typeArguments.isEmpty())
                return constantPool;
            else
                throw new InternalException(this + " is not a template class. Type arguments: " + typeArguments);
        }
//        typeArguments.forEach(Type::getTypeDesc);
        var typeMetadata = getExistingTypeMetadata(typeArguments);
        if (typeMetadata == constantPool)
            return constantPool;
        if(typeMetadata == null) {
            typeMetadata = createTypeMetadata(typeArguments);
            addTypeMetadata(typeMetadata);
        }
        else if (typeMetadata.getStage().isAfterOrAt(stage))
            return typeMetadata;
        var existingTm = typeMetadata;
        var subst = new SubstitutorV2(
                constantPool, getAllTypeParameters(), typeArguments, typeMetadata, stage);
        typeMetadata = (ConstantPool) constantPool.accept(subst);
        assert typeMetadata == existingTm;
        return typeMetadata;
    }

    public List<TypeVariable> getAllTypeParameters() {
        return typeParameters.toList();
    }


    public void update(List<Parameter> parameters, Type returnType) {
        setParameters(parameters, false);
        this.returnTypeIndex = constantPool.addValue(returnType);
        resetType();
    }

    public void foreachGenericDeclaration(Consumer<GenericDeclaration> action) {
        action.accept(this);
    }
}

