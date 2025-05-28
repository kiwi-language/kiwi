package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.entity.natives.RuntimeExceptionNative;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@NativeEntity(1)
@Entity
public class Method extends Flow implements Property {

    public static final Logger logger = LoggerFactory.getLogger(Method.class);

    private static final Pattern GETTER_PTN = Pattern.compile("(get|is)([A-Z][a-zA-Z0-9]*)");
    private static final Pattern SETTER_PTN = Pattern.compile("set([A-Z][a-zA-Z0-9]*)");
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private @NotNull Klass declaringType;
    private boolean _static;
    private Access access;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean hidden;

    private transient String nativeName;
    private transient volatile MethodHandle nativeHandle;

    public Method(@NotNull Id id,
                  @NotNull Klass declaringType,
                  String name,
                  boolean isConstructor,
                  boolean isAbstract,
                  boolean isNative,
                  boolean isSynthetic,
                  List<NameAndType> parameters,
                  int returnTypeIndex,
                  List<TypeVariable> typeParameters,
                  boolean isStatic,
                  Access access,
                  boolean hidden,
                  MetadataState state) {
        super(id, name, isNative, isSynthetic, returnTypeIndex, List.of(), state);
        if (isStatic && isAbstract)
            throw new BusinessException(ErrorCode.STATIC_FLOW_CAN_NOT_BE_ABSTRACT);
        this.declaringType = declaringType;
        setTypeParameters(typeParameters);
        this._static = isStatic;
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        this.access = access;
        this.hidden = hidden;
        var root = declaringType.getRoot();
        setParameters(Utils.map(parameters, p -> new Parameter(root.nextChildId(), p.name(), p.type(), this)));
        declaringType.addMethod(this);
        resetBody();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Flow.visitBody(visitor);
        visitor.visitBoolean();
        visitor.visitByte();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getName() + "("
                + Utils.join(getParameterTypes(),
                t -> t.toExpression(typeDef -> context.getModelName((org.metavm.entity.Entity) typeDef, this)))
                + ")";
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public @NotNull Klass getDeclaringType() {
        return declaringType;
    }

    @Override
    public Access getAccess() {
        return access;
    }

    @Override
    public void setAccess(Access access) {
        this.access = access;
    }

    @Override
    public boolean isStatic() {
        return _static;
    }

    @JsonIgnore
    public boolean isVirtual() {
        return !_static && !isConstructor && access != Access.PRIVATE;
    }

    @Override
    public void setStatic(boolean _static) {
        this._static = _static;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return declaringType;
    }

    @JsonIgnore
    public boolean isInstanceMethod() {
        return !isStatic();
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
        resetBody();
    }

    public boolean isHidden() {
        return hidden;
    }

    @JsonIgnore
    public String getQualifiedSignature() {
        return declaringType.getTypeDesc() + "." + getSignatureString();
    }

    public @Nullable FunctionType getStaticType() {
        return getStaticType(getConstantPool());
    }

    public @Nullable FunctionType getStaticType(TypeMetadata typeMetadata) {
        return isStatic() ? null : new FunctionType(
                Utils.prepend(declaringType.getType(), getParameterTypes(typeMetadata)),
                getReturnType(typeMetadata)
        );
    }

    @Override
    public String toString() {
        return "Method " + getQualifiedSignature();
    }

    @Override
    public FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext) {
//        logger.debug("Executing method: {}, self: {}, arguments: {}",
//                getQualifiedSignature(), self, arguments);
        try (var ignored = ContextUtil.getProfiler().enter("Method.execute: " + getDeclaringType().getName() + "." + getName())) {
            if (DebugEnv.debugging) {
                logger.info("Method.execute: {}", this);
                logger.info("Arguments: ");
                arguments.forEach(arg -> debugLogger.info(arg.getText()));
                logger.info(getText());
            }
            if (_static)
                Utils.require(self == null);
            else
                Objects.requireNonNull(self);
            checkArguments(arguments, flowRef.getTypeMetadata());
            FlowExecResult result;
            if (isNative())
                result = NativeMethods.invoke(this, self, arguments, callContext);
            else {
                if (!isCodePresent())
                    throw new InternalException("fail to invoke method: " + getQualifiedName() + ". root scope not present");
                try {
                    Value[] argArray;
                    ClosureContext closureContext;
                    if(self == null) {
                        closureContext = null;
                        argArray = arguments.toArray(Value[]::new);
                    } else {
                        closureContext = self.getClosureContext();
                        argArray = new Value[arguments.size() + 1];
                        argArray[0] = self;
                        int i = 1;
                        for (Value argument : arguments) {
                            argArray[i++] = argument;
                        }
                    }
                    result = VmStack.execute(
                            flowRef,
                            argArray,
                            closureContext,
                            callContext
                    );
                } catch (Exception e) {
                    logger.info("Failed to execute method {}", getQualifiedName());
//                    logger.info(getText());
                    throw new InternalException("Failed to execute method " + getQualifiedName(), e);
                }
            }
            if (isConstructor && result.ret() != null) {
                var instance = result.ret().resolveObject();
                var uninitializedField = instance.findUninitializedField(declaringType);
                if (uninitializedField != null) {
                    var exception = ClassInstance.allocate(TmpId.random(), StdKlass.runtimeException.get().getType());
                    var exceptionNative = new RuntimeExceptionNative(exception);
                    exceptionNative.RuntimeException(Instances.stringInstance(
                                    "Failed to instantiate " + instance.getInstanceType().getTypeDesc() + "，" +
                                            "field " + uninitializedField.getName() + " was not initialized"),
                            callContext);
                    return new FlowExecResult(null, exception);
                }
            }
            return result;
        }
    }

    @Override
    public String getQualifiedName() {
        return declaringType.getTypeDesc() + "." + getName();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if (current == this)
            return "this";
        return declaringType.getInternalName(null) + "." + getName() + "(" +
                Utils.join(getParameterTypes(getConstantPool()), type -> type.getInternalName(this)) + ")";
    }

    @Override
    public MethodRef getRef() {
        return new MethodRef(declaringType.getType(), this, List.of());
    }

    @Override
    public int getMinLocals() {
        return isStatic() ? getParameters().size() : getParameters().size() + 1;
    }

    @JsonIgnore
    public boolean isGetter() {
        if (!isPublic())
            return false;
        var name = getName();
        return GETTER_PTN.matcher(name).matches() && getParameters().isEmpty() && !getReturnType().isVoid();
    }

    @JsonIgnore
    public String getPropertyName() {
        var name = getName();
        var matcher = GETTER_PTN.matcher(name);
        if (matcher.matches())
            return NamingUtils.firstCharToLowerCase(matcher.group(2));
        matcher = SETTER_PTN.matcher(name);
        if(matcher.matches())
            return NamingUtils.firstCharToLowerCase(matcher.group(1));
        throw new IllegalStateException("Method " + getQualifiedName() + " is not a getter or setter");
    }

    @JsonIgnore
    public boolean isSetter() {
        if (!isPublic())
            return false;
        var name = getName();
        return SETTER_PTN.matcher(name).matches() && getParameters().size() == 1;
    }

    @Override
    public String getTypeDesc() {
        return getQualifiedName();
    }

    @Override
    public int getInputCount() {
        return _static || isConstructor ? getParameters().size() : 1 + getParameters().size();
    }

    @Override
    public int getTypeInputCount() {
        return 1 + super.getTypeInputCount();
    }

    public void setDeclaringType(@NotNull Klass klass) {
        this.declaringType = klass;
    }

    @Override
    public boolean hasBody() {
        return super.hasBody() && !isAbstract;
    }

    public static final int FLAG_CONSTRUCTOR = 8;
    public static final int FLAG_ABSTRACT = 16;
    public static final int FLAG_STATIC = 32;
    public static final int FLAG_HIDDEN = 64;

    public int getFlags() {
        int flags = super.getFlags();
        if(isConstructor) flags |= FLAG_CONSTRUCTOR;
        if(isAbstract) flags |= FLAG_ABSTRACT;
        if(_static) flags |= FLAG_STATIC;
        if(hidden) flags |= FLAG_HIDDEN;
        return flags;
    }

    public void setFlags(int flags) {
        super.setFlags(flags);
        isConstructor = (flags & FLAG_CONSTRUCTOR) != 0;
        isAbstract = (flags & FLAG_ABSTRACT) != 0;
        _static = (flags & FLAG_STATIC) != 0;
        hidden = (flags & FLAG_HIDDEN) != 0;
        resetBody();
    }

    @Override
    public List<TypeVariable> getAllTypeParameters() {
        var typeParams = new ArrayList<>(declaringType.getAllTypeParameters());
        typeParams.addAll(getTypeParameters());
        return typeParams;
    }

    @Override
    public boolean isConstantPoolParameterized() {
        return super.isConstantPoolParameterized() || declaringType.isConstantPoolParameterized();
    }

    @Override
    public void foreachGenericDeclaration(Consumer<GenericDeclaration> action) {
        declaringType.foreachGenericDeclaration(action);
        super.foreachGenericDeclaration(action);
    }


    @JsonIgnore
    public String getNativeName() {
        if (nativeName == null) {
            if (!getParameters().isEmpty() && Utils.count(declaringType.getMethods(),
                            m -> m.getName().equals(getName()) && m.getParameters().size() == getParameters().size()) > 1)
                nativeName = getName() + "__" + Utils.join(getParameterTypes(), t -> t.getUnderlyingType().getName(), "_");
            else
                nativeName = getName();
        }
        return nativeName;
    }

    @JsonIgnore
    public MethodHandle getNativeHandle() {
        return nativeHandle;
    }

    public void setNativeHandle(MethodHandle nativeHandle) {
        this.nativeHandle = nativeHandle;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethod(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("constructor", this.isConstructor());
        map.put("abstract", this.isAbstract());
        map.put("declaringType", this.getDeclaringType().getStringId());
        map.put("access", this.getAccess().name());
        map.put("static", this.isStatic());
        map.put("hidden", this.isHidden());
        var staticType = this.getStaticType();
        if (staticType != null) map.put("staticType", staticType.toJson());
        map.put("minLocals", this.getMinLocals());
        map.put("flags", this.getFlags());
        map.put("parameterTypes", this.getParameterTypes().stream().map(Type::toJson).toList());
        map.put("returnType", this.getReturnType().toJson());
        map.put("code", this.getCode().toJson());
        map.put("synthetic", this.isSynthetic());
        map.put("name", this.getName());
        map.put("state", this.getState().name());
        map.put("functionType", this.getFunctionType().toJson());
        map.put("native", this.isNative());
        map.put("typeParameters", this.getTypeParameters().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("parameters", this.getParameters().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("returnTypeIndex", this.getReturnTypeIndex());
        map.put("type", this.getType().toJson());
        map.put("capturedTypeVariables", this.getCapturedTypeVariables().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("lambdas", this.getLambdas().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("constantPool", this.getConstantPool().toJson());
        map.put("klasses", this.getKlasses().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("internalName", this.getInternalName());
        map.put("attributes", this.getAttributes().stream().map(Attribute::toJson).toList());
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
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Method;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.declaringType = (Klass) parent;
        this._static = input.readBoolean();
        this.access = Access.fromCode(input.read());
        this.isConstructor = input.readBoolean();
        this.isAbstract = input.readBoolean();
        this.hidden = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeBoolean(_static);
        output.write(access.code());
        output.writeBoolean(isConstructor);
        output.writeBoolean(isAbstract);
        output.writeBoolean(hidden);
    }

    @Override
    public void writeCode(CodeWriter writer) {
        if (isStatic())
            writer.write("static ");
        super.writeCode(writer);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}
