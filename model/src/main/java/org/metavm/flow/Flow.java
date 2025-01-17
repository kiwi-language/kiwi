package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.natives.CallContext;
import org.metavm.expression.VoidStructuralVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NativeEntity(28)
@Entity
@Slf4j
public abstract class Flow extends AttributedElement implements GenericDeclaration, Callable, LoadAware, CapturedTypeScope, ITypeDef, ConstantScope, KlassDeclaration{

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");
    @SuppressWarnings("unused")
    private static Klass __klass__;

    @EntityField(asTitle = true)
    private @NotNull String name;
    private boolean isNative;
    private boolean isSynthetic;
    private List<Parameter> parameters = new ArrayList<>();
    private int returnTypeIndex;
    private @Nullable Code code;
    private transient long version;
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isTemplate;
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private @NotNull MetadataState state;
    private int typeIndex;
    private List<CapturedTypeVariable> capturedTypeVariables = new ArrayList<>();
    private List<Lambda> lambdas = new ArrayList<>();
    private List<Klass> klasses = new ArrayList<>();
    private ConstantPool constantPool = new ConstantPool(this);

    private transient ResolutionStage stage = ResolutionStage.INIT;
    private transient Set<String> nodeNames = new HashSet<>();

    public Flow(Long tmpId,
                @NotNull String name,
                boolean isNative,
                boolean isSynthetic,
                List<NameAndType> parameters,
                @NotNull Type returnType,
                List<TypeVariable> typeParameters,
                @NotNull MetadataState state
    ) {
        super(tmpId);
        this.name = NamingUtils.ensureValidName(name);
        this.isNative = isNative;
        this.isSynthetic = isSynthetic;
        this.returnTypeIndex = constantPool.addValue(returnType);
        this.typeIndex = constantPool.addValue(new FunctionType(Utils.map(parameters, NameAndType::type), returnType));
        setTypeParameters(typeParameters);
        setParameters(Utils.map(parameters, p -> new Parameter(null, p.name(), p.type(), this)), false);
        this.state = state;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        AttributedElement.visitBody(visitor);
        visitor.visitUTF();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitList(visitor::visitEntity);
        visitor.visitInt();
        visitor.visitNullable(visitor::visitEntity);
        visitor.visitBoolean();
        visitor.visitList(visitor::visitEntity);
        visitor.visitByte();
        visitor.visitInt();
        visitor.visitList(visitor::visitEntity);
        visitor.visitList(visitor::visitEntity);
        visitor.visitList(visitor::visitEntity);
        visitor.visitEntity();
    }

    public abstract FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext);

    public List<Type> getParameterTypes(TypeMetadata typeMetadata) {
        return Utils.map(parameters, p -> p.getType(typeMetadata));
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

    @JsonIgnore
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
    }

    @JsonIgnore
    public boolean isError() {
        return getState() == MetadataState.ERROR;
    }

    public void clearContent() {
        clearNodes();
        constantPool.clear();
        capturedTypeVariables.clear();
    }

    public void clearNodes() {
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

    @Override
    public @NotNull String getName() {
        return name;
    }

    @JsonIgnore
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

    void addNode(Node node) {
        nodeNames.add(node.getName());
        version++;
    }

    void removeNode(Node node) {
        nodeNames.remove(node.getName());
        version++;
    }

    public Parameter getParameterByName(String name) {
        return Utils.findRequired(parameters, p -> p.getName().equals(name));
    }

    @Override
    public FunctionType getFunctionType() {
        return getType();
    }

    public long getVersion() {
        return version;
    }

    public boolean isNative() {
        return isNative;
    }

    public List<TypeVariable> getTypeParameters() {
        return Utils.listOf(typeParameters);
    }

    public void setParameters(List<Parameter> parameters) {
        setParameters(parameters, true);
    }

    private void setParameters(List<Parameter> parameters, boolean resetType) {
        parameters.forEach(p -> p.setCallable(this));
        this.parameters.clear();
        this.parameters.addAll(parameters);
        if (resetType)
            resetType();
    }

    protected void resetType() {
        typeIndex = constantPool.addValue(new FunctionType(Utils.map(parameters, Parameter::getType), constantPool.getType(returnTypeIndex)));
    }

    @Override
    public int getTypeIndex() {
        return typeIndex;
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.add(typeParameter);
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    @Override
    @JsonIgnore
    public int getInputCount() {
        return parameters.size();
    }

    @JsonIgnore
    public int getTypeInputCount() {
        return typeParameters.size();
    }

    public @Nullable Parameter findParameter(Predicate<Parameter> predicate) {
        return Utils.find(parameters, predicate);
    }

    public Parameter getParameter(Predicate<Parameter> predicate) {
        return Objects.requireNonNull(findParameter(predicate),
                "Can not find parameter in flow " + this + " with predicate");
    }

    public void setReturnType(Type returnType) {
        this.returnTypeIndex = constantPool.addValue(returnType);
        resetType();
    }

    @JsonIgnore
    public List<Type> getDefaultTypeArguments() {
        return Utils.map(typeParameters, TypeVariable::getType);
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
        resetBody();
    }

    @JsonIgnore
    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> {
            if (tp.getGenericDeclaration() != this)
                tp.setGenericDeclaration(this);
        });
        this.typeParameters.clear();
        this.typeParameters.addAll(typeParameters);
    }

    public void setCapturedTypeVariables(List<CapturedTypeVariable> capturedTypeVariables) {
        capturedTypeVariables.forEach(ct -> {
            if (ct.getScope() != this)
                ct.setScope(this);
        });
        this.capturedTypeVariables.clear();
        this.capturedTypeVariables.addAll(capturedTypeVariables);
    }

    public @NotNull FunctionType getType() {
        return constantPool.getFunctionType(typeIndex);
    }

    public FunctionType getType(TypeMetadata typeMetadata) {
        return typeMetadata.getFunctionType(typeIndex);
    }

    @JsonIgnore
    public String getSignatureString() {
        return name + "(" + Utils.join(getParameterTypes(), Type::getTypeDesc) + ")";
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
        return Collections.unmodifiableList(capturedTypeVariables);
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
        capturedTypeVariables.add(capturedTypeVariable);
    }

    @JsonIgnore
    public ResolutionStage getStage() {
        return stage();
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var curStage = this.stage;
        this.stage = stage;
        return curStage;
    }

    public void writeCode(CodeWriter writer) {
        writer.writeln(
                "Flow "
                        + name
                        + " (" + Utils.join(parameters, Parameter::getText, ", ")
                        + ")"
                        + ": " + getReturnType().getName()
        );
        if (isCodePresent())
            getCode().writeCode(writer);
        else
            writer.write(";");
    }

    @JsonIgnore
    public String getText() {
        CodeWriter writer = new CodeWriter();
        writeCode(writer);
        return writer.toString();
    }

    @Override
    @JsonIgnore
    public String getScopeName() {
        return name;
    }

    @Override
    @JsonIgnore
    public String getTypeDesc() {
        return name;
    }

    private ResolutionStage stage() {
        if(stage == null)
            stage = ResolutionStage.INIT;
        return stage;
    }

    @JsonIgnore
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
        return Collections.unmodifiableList(lambdas);
    }

    public void addLambda(Lambda lambda) {
        this.lambdas.add(lambda);
        lambda.setFlow(this);
    }

    public void setLambdas(List<Lambda> lambdas) {
        this.lambdas.clear();
        this.lambdas.addAll(lambdas);
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
            code = new Code(this);
        else
            code = null;
    }

    public void addLocalKlass(Klass localKlass) {
        klasses.add(localKlass);
        localKlass.setScope(this);
    }

    public List<Klass> getKlasses() {
        return Collections.unmodifiableList(klasses);
    }

    public void setKlasses(List<Klass> klasses) {
        this.klasses.clear();
        this.klasses.addAll(klasses);
        klasses.forEach(k -> k.setScope(this));
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

    public ConstantPool getExistingTypeMetadata(List<? extends Type> typeArguments) {
        if (Utils.map(getAllTypeParameters(), TypeVariable::getType).equals(typeArguments))
            return constantPool;
        return (ConstantPool) ParameterizedStore.get(this, typeArguments);
    }

    private ConstantPool createTypeMetadata(List<? extends Type> typeArguments) {
        return new ConstantPool(this, typeArguments);
    }

    public void addTypeMetadata(ConstantPool parameterized) {
        var existing = ParameterizedStore.get(this, parameterized.typeArguments.secretlyGetTable());
        if(existing != null)
            throw new IllegalStateException("Parameterized klass " + parameterized + " already exists. "
                    + "existing: " + System.identityHashCode(existing) + ", new: "+ System.identityHashCode(parameterized)
            );
        Utils.require(ParameterizedStore.put(this, parameterized.typeArguments.secretlyGetTable(), parameterized) == null,
                () -> "Parameterized klass " + parameterized + " already exists");
    }

    @JsonIgnore
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

    @JsonIgnore
    public List<TypeVariable> getAllTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }


    public void update(List<Parameter> parameters, Type returnType) {
        setParameters(parameters, false);
        this.returnTypeIndex = constantPool.addValue(returnType);
        resetType();
    }

    public void foreachGenericDeclaration(Consumer<GenericDeclaration> action) {
        action.accept(this);
    }

    @Override
    public String getTitle() {
        return getQualifiedName();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        parameters.forEach(arg -> arg.accept(visitor));
        if (code != null) code.accept(visitor);
        typeParameters.forEach(arg -> arg.accept(visitor));
        capturedTypeVariables.forEach(arg -> arg.accept(visitor));
        lambdas.forEach(arg -> arg.accept(visitor));
        klasses.forEach(arg -> arg.accept(visitor));
        constantPool.accept(visitor);
    }

    private void onRead() {
        stage = ResolutionStage.INIT;
        nodeNames = new HashSet<>();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        parameters.forEach(arg -> action.accept(arg.getReference()));
        if (code != null) action.accept(code.getReference());
        typeParameters.forEach(arg -> action.accept(arg.getReference()));
        capturedTypeVariables.forEach(arg -> action.accept(arg.getReference()));
        lambdas.forEach(arg -> action.accept(arg.getReference()));
        klasses.forEach(arg -> action.accept(arg.getReference()));
        action.accept(constantPool.getReference());
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("parameterTypes", this.getParameterTypes().stream().map(Type::toJson).toList());
        map.put("returnType", this.getReturnType().toJson());
        map.put("code", this.getCode().getStringId());
        map.put("synthetic", this.isSynthetic());
        map.put("name", this.getName());
        map.put("state", this.getState().name());
        map.put("functionType", this.getFunctionType().toJson());
        map.put("native", this.isNative());
        map.put("typeParameters", this.getTypeParameters().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("typeIndex", this.getTypeIndex());
        map.put("parameters", this.getParameters().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("type", this.getType().toJson());
        map.put("capturedTypeVariables", this.getCapturedTypeVariables().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("lambdas", this.getLambdas().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("constantPool", this.getConstantPool().getStringId());
        map.put("klasses", this.getKlasses().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("flags", this.getFlags());
        map.put("attributes", this.getAttributes().stream().map(Attribute::toJson).toList());
        map.put("minLocals", this.getMinLocals());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
        parameters.forEach(action);
        if (code != null) action.accept(code);
        typeParameters.forEach(action);
        capturedTypeVariables.forEach(action);
        lambdas.forEach(action);
        klasses.forEach(action);
        action.accept(constantPool);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Flow;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.name = input.readUTF();
        this.isNative = input.readBoolean();
        this.isSynthetic = input.readBoolean();
        this.parameters = input.readList(() -> input.readEntity(Parameter.class, this));
        this.returnTypeIndex = input.readInt();
        this.code = input.readNullable(() -> input.readEntity(Code.class, this));
        this.isTemplate = input.readBoolean();
        this.typeParameters = input.readList(() -> input.readEntity(TypeVariable.class, this));
        this.state = MetadataState.fromCode(input.read());
        this.typeIndex = input.readInt();
        this.capturedTypeVariables = input.readList(() -> input.readEntity(CapturedTypeVariable.class, this));
        this.lambdas = input.readList(() -> input.readEntity(Lambda.class, this));
        this.klasses = input.readList(() -> input.readEntity(Klass.class, this));
        this.constantPool = input.readEntity(ConstantPool.class, this);
        this.onRead();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeUTF(name);
        output.writeBoolean(isNative);
        output.writeBoolean(isSynthetic);
        output.writeList(parameters, output::writeEntity);
        output.writeInt(returnTypeIndex);
        output.writeNullable(code, output::writeEntity);
        output.writeBoolean(isTemplate);
        output.writeList(typeParameters, output::writeEntity);
        output.write(state.code());
        output.writeInt(typeIndex);
        output.writeList(capturedTypeVariables, output::writeEntity);
        output.writeList(lambdas, output::writeEntity);
        output.writeList(klasses, output::writeEntity);
        output.writeEntity(constantPool);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

