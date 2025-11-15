package org.metavm.flow;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.JsonIgnore;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ExceptionNative;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.metavm.wire.Parent;
import org.metavm.wire.Wire;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Wire(1)
@Entity
@Slf4j
public class Method extends Flow implements Property {

    private static final Pattern GETTER_PTN = Pattern.compile("(get|is)([A-Z][a-zA-Z0-9]*)");
    private static final Pattern SETTER_PTN = Pattern.compile("set([A-Z][a-zA-Z0-9]*)");

    @Parent
    private @NotNull Klass declaringType;
    private boolean _static;
    private Access access;
    private boolean isConstructor;
    private boolean isAbstract;
    @Getter
    private boolean hidden;

    @Getter
    private final transient NativeFunction nativeFunction;

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
                  NativeFunction nativeFunction, MetadataState state) {
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
        this.nativeFunction = nativeFunction;
        var root = declaringType.getRoot();
        setParameters(Utils.map(parameters, p -> new Parameter(root.nextChildId(), p.name(), p.type(), this)));
        declaringType.addMethod(this);
        resetBody();
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

    @JsonIgnore
    public String getQualifiedSignature() {
        return declaringType.getTypeDesc() + "." + getSignatureString();
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
        try (var ignored = ContextUtil.getProfiler().enter("Method.execute: " + getDeclaringType().getName() + "." + getName())) {
            if (DebugEnv.debugging) {
                log.info("Method.execute: {}", this);
                log.info("Arguments: ");
                arguments.forEach(arg -> log.info(arg.getText()));
                log.info(getText());
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
                } catch (BusinessException e) {
                    throw e;
                } catch (Exception e) {
                    log.info("Failed to execute method {}", getQualifiedName());
//                    logger.info(getText());
                    throw new InternalException("Failed to execute method " + getQualifiedName(), e);
                }
            }
            if (isConstructor && result.ret() != null) {
                var instance = result.ret().resolveObject();
                var uninitializedField = instance.findUninitializedField(declaringType);
                if (uninitializedField != null) {
                    var exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.get().getType());
                    ExceptionNative.Exception(exception, Instances.stringInstance(
                                    "Failed to instantiate " + instance.getInstanceType().getTypeDesc() + "，" +
                                            "field " + uninitializedField.getName() + " was not initialized")
                    );
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
            return NamingUtils.firstCharsToLowerCase(matcher.group(2));
        matcher = SETTER_PTN.matcher(name);
        if(matcher.matches())
            return NamingUtils.firstCharsToLowerCase(matcher.group(1));
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

    public int getFlags() {
        int flags = super.getFlags();
        if(isConstructor) flags |= MethodFlags.FLAG_CONSTRUCTOR;
        if(isAbstract) flags |= MethodFlags.FLAG_ABSTRACT;
        if(_static) flags |= MethodFlags.FLAG_STATIC;
        if(hidden) flags |= MethodFlags.FLAG_HIDDEN;
        return flags;
    }

    public void setFlags(int flags) {
        super.setFlags(flags);
        isConstructor = (flags & MethodFlags.FLAG_CONSTRUCTOR) != 0;
        isAbstract = (flags & MethodFlags.FLAG_ABSTRACT) != 0;
        _static = (flags & MethodFlags.FLAG_STATIC) != 0;
        hidden = (flags & MethodFlags.FLAG_HIDDEN) != 0;
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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public void writeCode(CodeWriter writer) {
        if (isStatic())
            writer.write("static ");
        super.writeCode(writer);
    }

}
