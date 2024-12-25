package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.DefaultCallContext;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.entity.natives.RuntimeExceptionNative;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Entity
public class Method extends Flow implements Property {

    public static final Logger logger = LoggerFactory.getLogger(Method.class);

    private static final Pattern GETTER_PTN = Pattern.compile("(get|is)([A-Z][a-zA-Z0-9]*)");
    private static final Pattern SETTER_PTN = Pattern.compile("set([A-Z][a-zA-Z0-9]*)");

    private @NotNull Klass declaringType;
    private boolean _static;
    private Access access;
    private boolean isConstructor;
    private boolean isAbstract;
    private int staticTypeIndex;

    private boolean hidden;

    private transient String nativeName;
    private transient volatile MethodHandle nativeHandle;

    public Method(Long tmpId,
                  @NotNull Klass declaringType,
                  String name,
                  boolean isConstructor,
                  boolean isAbstract,
                  boolean isNative,
                  boolean isSynthetic,
                  List<NameAndType> parameters,
                  Type returnType,
                  List<TypeVariable> typeParameters,
                  boolean isStatic,
                  Access access,
                  @Nullable CodeSource codeSource,
                  boolean hidden,
                  MetadataState state) {
        super(tmpId, name, isNative, isSynthetic, parameters, returnType, List.of(), codeSource, state);
        if (isStatic && isAbstract)
            throw new BusinessException(ErrorCode.STATIC_FLOW_CAN_NOT_BE_ABSTRACT);
        this.declaringType = declaringType;
        setTypeParameters(typeParameters);
        this._static = isStatic;
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        staticTypeIndex = isStatic ? -1 : getConstantPool().addValue(new FunctionType(
                NncUtils.prepend(declaringType.getType(), NncUtils.map(parameters, NameAndType::type)),
                returnType
        ));
        this.access = access;
        this.hidden = hidden;
        declaringType.addMethod(this);
        checkTypes(getParameters(), returnType);
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
                + NncUtils.join(getParameterTypes(),
                t -> t.toExpression(typeDef -> context.getModelName(typeDef, this)))
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

    public boolean isVirtual() {
        return !_static && !isConstructor && access != Access.PRIVATE;
    }

    @Override
    public void setStatic(boolean _static) {
        this._static = _static;
    }

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        declaringType.rebuildMethodTable();
        declaringType.removeErrors(this);
        return super.beforeRemove(context);
    }

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

    public String getQualifiedSignature() {
        return declaringType.getTypeDesc() + "." + getSignatureString();
    }

    public @Nullable FunctionType getStaticType() {
        return staticTypeIndex == -1 ? null : getConstantPool().getFunctionType(staticTypeIndex);
    }

    public int getStaticTypeIndex() {
        return staticTypeIndex;
    }

    @Override
    protected String toString0() {
        return getQualifiedSignature();
    }

    private void checkTypes(List<Parameter> parameters, Type returnType) {
        var paramTypes = NncUtils.map(parameters, Parameter::getType);
        var staticType = getStaticType();
        if (isInstanceMethod()) {
            AssertUtils.assertNonNull(staticType, ErrorCode.INSTANCE_METHOD_MISSING_STATIC_TYPE);
            if (!staticType.getParameterTypes().equals(NncUtils.prepend(declaringType.getType(), paramTypes))
                    || !staticType.getReturnType().equals(returnType))
                throw new InternalException("Incorrect static function type: " + staticType);
        } else
            NncUtils.requireNull(staticType);
    }

    @Override
    protected void resetType() {
        super.resetType();
        staticTypeIndex = isStatic() ? - 1 : getConstantPool().addValue(new FunctionType(
                NncUtils.prepend(declaringType.getType(), getParameterTypes()),
                getReturnType()
        ));
    }

    @Override
    public FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext) {
//        logger.debug("Executing method: {}, self: {}, arguments: {}",
//                getQualifiedSignature(), self, arguments);
        try (var ignored = ContextUtil.getProfiler().enter("Method.execute: " + getDeclaringType().getName() + "." + getName())) {
            if (DebugEnv.debugging) {
                logger.debug("Method.execute: {}", this);
                logger.debug("Arguments: ");
                arguments.forEach(arg -> debugLogger.info(arg.getText()));
                logger.debug(getText());
            }
            if (_static)
                NncUtils.requireNull(self);
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
                            new DefaultCallContext(callContext.instanceRepository())
                    );
                } catch (Exception e) {
                    logger.info("Fail to execute method {}", getQualifiedName());
//                    logger.info(getText());
                    throw new InternalException("fail to execute method " + getQualifiedName(), e);
                }
            }
            if (isConstructor && result.ret() != null) {
                var instance = result.ret().resolveObject();
                var uninitializedField = instance.findUninitializedField(declaringType);
                if (uninitializedField != null) {
                    var exception = ClassInstance.allocate(StdKlass.runtimeException.get().getType());
                    var exceptionNative = new RuntimeExceptionNative(exception);
                    exceptionNative.RuntimeException(Instances.stringInstance(
                                    "Failed to instantiate " + instance.getType().getTypeDesc() + "，" +
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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethod(this);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        if (current == this)
            return "this";
        return declaringType.getInternalName(null) + "." + getName() + "(" +
                NncUtils.join(getParameterTypes(getConstantPool()), type -> type.getInternalName(this)) + ")";
    }

    @Override
    public MethodRef getRef() {
        return new MethodRef(declaringType.getType(), this, List.of());
    }

    @Override
    public int getMinLocals() {
        return isStatic() ? getParameters().size() : getParameters().size() + 1;
    }

    public boolean isGetter() {
        if (!isPublic())
            return false;
        var name = getName();
        return GETTER_PTN.matcher(name).matches() && getParameters().isEmpty() && !getReturnType().isVoid();
    }

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
        setDeclaringType(klass, true);
    }

    public void setDeclaringType(@NotNull Klass klass, boolean resetType) {
        this.declaringType = klass;
        if(resetType)
            resetType();
    }

    @Override
    public boolean hasBody() {
        return super.hasBody() && !isAbstract;
    }

    public static final int FLAG_CONSTRUCTOR = 4;
    public static final int FLAG_ABSTRACT = 8;
    public static final int FLAG_STATIC = 16;
    public static final int FLAG_HIDDEN = 32;

    public int getFlags() {
        int flags = super.getFlags();
        if(isConstructor)
            flags |= FLAG_CONSTRUCTOR;
        if(isAbstract)
            flags |= FLAG_ABSTRACT;
        if(_static)
            flags |= FLAG_STATIC;
        if(hidden)
            flags |= FLAG_HIDDEN;
        return flags;
    }

    void setFlags(int flags) {
        super.setFlags(flags);
        isConstructor = (flags & FLAG_CONSTRUCTOR) != 0;
        isAbstract = (flags & FLAG_ABSTRACT) != 0;
        _static = (flags & FLAG_STATIC) != 0;
        hidden = (flags & FLAG_HIDDEN) != 0;
    }

    public void write(MvOutput output) {
        super.write(output);
        output.write(access.code());
        output.writeShort(staticTypeIndex);
    }

    public void read(KlassInput input) {
        super.read(input);
        access = Access.fromCode(input.read());
        staticTypeIndex = (short) input.readShort();
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


    public String getNativeName() {
        if (nativeName == null) {
            if (!getParameters().isEmpty() && NncUtils.count(declaringType.getMethods(),
                            m -> m.getName().equals(getName()) && m.getParameters().size() == getParameters().size()) > 1)
                nativeName = getName() + "__" + NncUtils.join(getParameterTypes(), t -> t.getUnderlyingType().getName(), "_");
            else
                nativeName = getName();
        }
        return nativeName;
    }

    public MethodHandle getNativeHandle() {
        return nativeHandle;
    }

    public void setNativeHandle(MethodHandle nativeHandle) {
        this.nativeHandle = nativeHandle;
    }

}
