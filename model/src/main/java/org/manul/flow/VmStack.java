package org.manul.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.manul.entity.StdKlass;
import org.manul.entity.natives.CallContext;
import org.manul.entity.natives.ExceptionNative;
import org.manul.entity.natives.NativeMethods;
import org.manul.object.instance.IndexKeyRT;
import org.manul.object.instance.core.Value;
import org.manul.object.instance.core.*;
import org.manul.object.type.*;
import org.manul.object.type.generic.TypeSubstitutor;
import java.util.LinkedList;
import org.manul.util.*;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class VmStack {

    private static final ObjectPool<VmStack> pool = new ObjectPool<>(1024, VmStack::new);

    public static FlowExecResult execute(CallableRef callableRef,
                                         Value[] arguments,
                                         @Nullable ClosureContext closureContext,
                                         CallContext callContext) {
        var stack = pool.borrowObject();
        try {
            return stack.execute0(callableRef, arguments, closureContext, callContext);
        }
        finally {
            pool.returnObject(stack);
        }
    }

    private final ExceptionHandler[] exceptionHandlers = new ExceptionHandler[1024];
    private final Value[] stack = new Value[1024 * 1024];
    private final long[] pstack = new long[1024 * 1024];
    private final byte[] ptag = new byte[1024 * 1024];
    private final Frame[] frames = new Frame[1024];

    private static final byte TAG_OBJ = 0;
    private static final byte TAG_INT = 1;
    private static final byte TAG_LONG = 2;
    private static final byte TAG_FLOAT = 3;
    private static final byte TAG_DOUBLE = 4;

    private VmStack() {
    }

    private void ensureBoxed(int i) {
        if (ptag[i] != TAG_OBJ) {
            stack[i] = boxFromTag(ptag[i], pstack[i]);
            ptag[i] = TAG_OBJ;
        }
    }

    private void ensureBoxedRange(int from, int to) {
        for (int i = from; i < to; i++) ensureBoxed(i);
    }

    private static Value boxFromTag(byte tag, long bits) {
        return switch (tag) {
            case TAG_INT -> new IntValue((int) bits);
            case TAG_LONG -> new LongValue(bits);
            case TAG_FLOAT -> new FloatValue(Float.intBitsToFloat((int) bits));
            case TAG_DOUBLE -> new DoubleValue(Double.longBitsToDouble(bits));
            default -> throw new IllegalStateException();
        };
    }

    private void unboxTo(int i, Value v) {
        if (v instanceof IntValue iv) {
            pstack[i] = iv.value; ptag[i] = TAG_INT; stack[i] = null;
        } else if (v instanceof LongValue lv) {
            pstack[i] = lv.value; ptag[i] = TAG_LONG; stack[i] = null;
        } else if (v instanceof FloatValue fv) {
            pstack[i] = Float.floatToRawIntBits(fv.value); ptag[i] = TAG_FLOAT; stack[i] = null;
        } else if (v instanceof DoubleValue dv) {
            pstack[i] = Double.doubleToRawLongBits(dv.value); ptag[i] = TAG_DOUBLE; stack[i] = null;
        } else {
            stack[i] = v; ptag[i] = TAG_OBJ;
        }
    }

    @SuppressWarnings({"DuplicatedCode", "UseCompareMethod", "DataFlowIssue", "ExtractMethodRecommender"})
    private @NotNull FlowExecResult execute0(CallableRef callableRef,
                                           Value[] arguments,
                                           @Nullable ClosureContext closureContext,
                                           CallContext callContext) {

//        if(DebugEnv.flag) {
//            log.debug("Executing flow {}, maxLocals: {}, maxStack: {}, constants: {}, code length: {}",
//                    scope.getCallable(), scope.getMaxLocals(), scope.getMaxStack(), scope.getConstantPool().getEntries().size(),
//                    scope.getCode().length);
//            log.debug("{}", EncodingUtils.bytesToHex(scope.getCode()));
//            log.debug("Constants: {}", Arrays.toString(scope.getConstantPool().getResolvedValues()));
//        }
        try {
            var constants = callableRef.getTypeMetadata().getValues();
            System.arraycopy(arguments, 0, stack, 0, arguments.length);
            var stack = this.stack;
            var pstack = this.pstack;
            var ptag = this.ptag;
            var frames = this.frames;
            int base = 0;
            var code = callableRef.getCode();
            int top = code.getMaxLocals();
            for (int i = 0; i < arguments.length; i++) unboxTo(i, arguments[i]);
            Arrays.fill(ptag, arguments.length, top, TAG_OBJ);
            int handlerTop = 0;
            int pc = 0;
            var bytes = code.getCode();
            var repository = Utils.safeCall(callContext, CallContext::instanceRepository);
            var fp = 0;
            ClassInstance exception;

            for (;;) {
                var b = bytes[pc] & 0xff;
                try {
//                    if(DebugEnv.flag)
//                        log.debug("Executing bytecode {} at {}, top: {}, callable: {}", Bytecodes.getBytecodeName(b), pc, top, callableRef);
                    except: {
                        switch (b) {
                            case Bytecodes.ADD_OBJECT -> {
                                int typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                boolean ephemeral = bytes[pc + 3] == 1;
                                var instance = ClassInstanceBuilder.newBuilder(type, repository.allocateRootId(type))
                                        .ephemeral(ephemeral)
                                        .build();
                                var fieldValues = new LinkedList<Value>();
                                var fields = type.getKlass().getAllFields();
                                int numFields = fields.size();
                                ensureBoxedRange(top - numFields, top);
                                for (int i = 0; i < numFields; i++) {
                                    fieldValues.addFirst(stack[--top]);
                                }
                                Utils.biForEach(fields, fieldValues, (f, v) -> instance.initField(f, f.getType().fromStackValue(v)));
                                if (!instance.isEphemeral())
                                    callContext.instanceRepository().bind(instance);
                                stack[top] = instance.getReference(); ptag[top++] = TAG_OBJ;
                                pc += 4;
                            }
                            case Bytecodes.SET_FIELD -> {
                                int fieldIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var field = (FieldRef) constants[fieldIndex];
                                ensureBoxed(top - 1);
                                var value = stack[--top];
                                var instance = stack[--top].resolveMvObject();
                                instance.fields[field.getRawField().offset].value = field.getPropertyType().fromStackValue(value);
                                pc += 3;
                            }
                            case Bytecodes.RETURN -> {
                                var retTag = ptag[top - 1]; var retPrim = pstack[top - 1]; var retObj = stack[top - 1];
                                Arrays.fill(stack, base, base + code.getFrameSize(), null);
                                Arrays.fill(ptag, base, base + code.getFrameSize(), TAG_OBJ);
                                if (fp == 0)
                                    return FlowExecResult.of(retTag != TAG_OBJ ? boxFromTag(retTag, retPrim) : retObj);
                                var frame = frames[--fp];
                                frames[fp] = null;
                                stack[frame.top] = retObj; pstack[frame.top] = retPrim; ptag[frame.top] = retTag;
                                pc = frame.pc;
                                top = frame.top + 1;
                                base = frame.base;
                                callableRef = frame.callableRef;
                                code = callableRef.getCode();
                                bytes = code.getCode();
                                constants = callableRef.getTypeMetadata().getValues();
                                closureContext = frame.closureContext;
                            }
                            case Bytecodes.RAISE -> {
                                exception = stack[--top].resolveObject();
                                break except;
                            }
                            case Bytecodes.INVOKE_VIRTUAL -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var method = (MethodRef) constants[flowIndex];
                                pc += 3;
                                var self = stack[top - method.getParameterCount() - 1];
                                if (method.isVirtual())
                                    method = ((ClassType) requireNonNull(self).getValueType()).getOverride(method);
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = method;
                                    code = method.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = method.getTypeMetadata().getValues();
                                    closureContext = stack[base].resolveObject().getClosureContext();
                                    pc = 0;
                                }
                            }
                            case Bytecodes.GENERIC_INVOKE_VIRTUAL -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var m = (Method) (((MethodRef) constants[flowIndex]).getRawFlow());
                                pc += 3;
                                var typeArgCnt = m.getTypeParameters().size();
                                var typeArgs = new Type[typeArgCnt];
                                for (int i = typeArgCnt - 1; i >= 0; i--) {
                                    typeArgs[i] = (Type) stack[--top];
                                }
                                var declaringType = (ClassType) stack[--top];
                                var method = new MethodRef(declaringType, m, List.of(typeArgs));
                                var self = stack[top - method.getParameterCount() - 1];
                                if (method.isVirtual())
                                    method = ((ClassType) requireNonNull(self).getValueType()).getOverride(method);
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = method;
                                    code = method.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = method.getTypeMetadata().getValues();
                                    closureContext = stack[base].resolveObject().getClosureContext();
                                    pc = 0;
                                }
                            }
                            case Bytecodes.INVOKE_SPECIAL -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var method = (MethodRef) constants[flowIndex];
                                pc += 3;
                                var self = stack[top - method.getParameterCount() - 1];
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = method;
                                    code = method.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = method.getTypeMetadata().getValues();
                                    closureContext = stack[base].resolveObject().getClosureContext();
                                    pc = 0;
                                }
                            }
                            case Bytecodes.GENERIC_INVOKE_SPECIAL -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var m = (Method) (((MethodRef) constants[flowIndex]).getRawFlow());
                                pc += 3;
                                var typeArgCnt = m.getTypeParameters().size();
                                var typeArgs = new Type[typeArgCnt];
                                for (int i = typeArgCnt - 1; i >= 0; i--) {
                                    typeArgs[i] = (Type) stack[--top];
                                }
                                var declaringType = (ClassType) stack[--top];
                                var method = new MethodRef(declaringType, m, List.of(typeArgs));
                                var self = stack[top - method.getParameterCount() - 1];
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = method;
                                    code = method.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = method.getTypeMetadata().getValues();
                                    closureContext = stack[base].resolveObject().getClosureContext();
                                    pc = 0;
                                }
                            }
                            case Bytecodes.INVOKE_STATIC -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var method = (MethodRef) constants[flowIndex];
                                pc += 3;
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    var r = NativeMethods.invoke(method.getRawFlow(), null, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - method.getParameterCount();
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = method;
                                    code = method.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = method.getTypeMetadata().getValues();
                                    closureContext = null;
                                    pc = 0;
                                }
                            }
                            case Bytecodes.GENERIC_INVOKE_STATIC -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var m = (Method) (((MethodRef) constants[flowIndex]).getRawFlow());
                                pc += 3;
                                var typeArgCnt = m.getTypeParameters().size();
                                var typeArgs = new Type[typeArgCnt];
                                for (int i = typeArgCnt - 1; i >= 0; i--) {
                                    typeArgs[i] = (Type) stack[--top];
                                }
                                var declaringType = (ClassType) stack[--top];
                                var method = new MethodRef(declaringType, m, List.of(typeArgs));
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    var r = NativeMethods.invoke(method.getRawFlow(), null, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - method.getParameterCount();
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = method;
                                    code = method.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = method.getTypeMetadata().getValues();
                                    closureContext = null;
                                    pc = 0;
                                }
                            }
                            case Bytecodes.GET_UNIQUE -> {
                                var index = ((IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff]);
                                Value result = callContext.instanceRepository().selectFirstByKey(loadIndexKey(index, stack[--top]));
                                if (result == null)
                                    result = new NullValue();
                                stack[top] = result; ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.NEW -> {
                                var typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                var ephemeral = bytes[pc + 3] == 1;
                                var unbound = bytes[pc + 4] == 1;
                                if (type.isLocal()) ensureBoxedRange(base, top);
                                var self = ClassInstanceBuilder.newBuilder(type, repository.allocateRootId(type))
                                        .ephemeral(ephemeral)
                                        .closureContext(type.isLocal() ? new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)) : null)
                                        .build();
                                if (!self.isEphemeral() && !unbound)
                                    callContext.instanceRepository().bind(self);
                                stack[top] = self.getReference(); ptag[top++] = TAG_OBJ;
                                pc += 5;
                            }
                            case Bytecodes.NEW_CHILD -> {
                                var typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                var parent = stack[--top].resolveObject();
                                if (type.isLocal()) ensureBoxedRange(base, top + 1);
                                var self = ClassInstanceBuilder.newBuilder(type, parent.getRoot().nextChildId())
                                        .parent(parent)
                                        .closureContext(type.isLocal() ? new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top + 1)) : null)
                                        .build();
                                stack[top] = self.getReference(); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.SET_STATIC -> {
                                var field = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                ensureBoxed(top - 1);
                                var sft = StaticFieldTable.getInstance(field.getDeclaringType(), ContextUtil.getEntityContext());
                                sft.set(field.getRawField(), stack[--top]);
                                pc += 3;
                            }
                            case Bytecodes.NEW_ARRAY -> {
                                // TODO support ephemeral
                                var array = new ArrayInstance((ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff]);
                                stack[top] = array.getReference(); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.TRY_ENTER -> {
                                var handler = pc + ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                exceptionHandlers[handlerTop++] = new ExceptionHandler(fp, handler);
                                pc += 3;
                            }
                            case Bytecodes.TRY_EXIT -> {
                                handlerTop--;
                                pc++;
                            }
                            case Bytecodes.FUNC -> {
                                var functionType = (FunctionType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                pc += 3;
                                int prevBase = base;
                                int savedTop = top;
                                int prevTop = top - functionType.getParameterTypes().size() - 1;
                                var funcInst = (FunctionValue) stack[prevTop];
                                var self = funcInst.getSelf();
                                if (self != null)
                                    stack[base = prevTop] = self;
                                else
                                    base = prevTop + 1;
                                top = base + funcInst.getCode().getMaxLocals();
                                if (savedTop < top) Arrays.fill(ptag, savedTop, top, TAG_OBJ);
                                frames[fp++] = new Frame(pc, prevBase, prevTop, callableRef, closureContext);
                                callableRef = funcInst;
                                code = funcInst.getCode();
                                bytes = code.getCode();
                                constants = funcInst.getTypeMetadata().getValues();
                                closureContext = funcInst.getClosureContext(stack, base);
                                pc = 0;
                            }
                            case Bytecodes.LAMBDA -> {
                                var lambda = (LambdaRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                ensureBoxedRange(base, top);
                                var func = new LambdaValue(lambda, new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)));
                                if (bytes[pc + 3] == 0) {
                                    stack[top] = func; ptag[top++] = TAG_OBJ;
                                    pc += 4;
                                } else {
                                    var functionalInterface = (ClassType) constants[bytes[pc + 4] | bytes[pc + 5]];
                                    // TODO Pre-generate functional interface implementation
                                    var functionInterfaceImpl = Types.createFunctionalClass(functionalInterface);
                                    var funcImplKlass = functionInterfaceImpl.getKlass();
                                    var funcField = funcImplKlass.getFieldByName("func");
                                    stack[top] = ClassInstance.create(TmpId.random(), Map.of(funcField, func), functionInterfaceImpl).getReference(); ptag[top++] = TAG_OBJ;
                                    pc += 6;
                                }
                            }
                            case Bytecodes.ADD_ELEMENT -> {
                                ensureBoxed(top - 1);
                                var e = stack[--top];
                                var a = stack[--top].resolveArray();
                                a.addElement(a.getInstanceType().getElementType().fromStackValue(e));
                                pc++;
                            }
                            case Bytecodes.DELETE_ELEMENT -> {
                                ensureBoxed(top - 1);
                                var elem = stack[--top];
                                var array = stack[--top].resolveArray();
                                var r = array.remove(elem);
                                stack[top] = null; pstack[top] = r ? 1 : 0; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.GET_ELEMENT -> {
                                var index = (int) pstack[--top];
                                var arrayInst = stack[--top].resolveArray();
                                if (index < arrayInst.size()) {
                                    unboxTo(top, arrayInst.get(index).toStackValue());
                                    top++;
                                    pc++;
                                } else {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("Index out of bound"));
                                    break except;
                                }
                            }
                            case Bytecodes.INVOKE_FUNCTION -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var func = (FunctionRef) constants[flowIndex];
                                pc += 3;
                                if (func.isNative()) {
                                    var paramCount = func.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    var nativeCode = Objects.requireNonNull(func.getRawFlow().getNativeCode());
                                    var r = nativeCode.run(func, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!func.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - func.getParameterCount();
                                    top = base + func.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = func;
                                    code = func.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = func.getTypeMetadata().getValues();
                                    closureContext = null;
                                    pc = 0;
                                }
                            }
                            case Bytecodes.GENERIC_INVOKE_FUNCTION -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var f = (Function) (((FunctionRef) constants[flowIndex]).getRawFlow());
                                pc += 3;
                                var typeArgCnt = f.getTypeParameters().size();
                                var typeArgs = new Type[typeArgCnt];
                                for (int i = typeArgCnt - 1; i >= 0; i--) {
                                    typeArgs[i] = (Type) stack[--top];
                                }
                                var func = new FunctionRef(f, List.of(typeArgs));
                                if (func.isNative()) {
                                    var paramCount = func.getParameterCount();
                                    for (int i = top - paramCount; i < top; i++) ensureBoxed(i);
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    var nativeCode = Objects.requireNonNull(func.getRawFlow().getNativeCode());
                                    var r = nativeCode.run(func, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!func.getReturnType().isVoid()) {
                                        unboxTo(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    int argsEnd = top;
                                    base = top - func.getParameterCount();
                                    top = base + func.getRawFlow().getCode().getMaxLocals();
                                    if (argsEnd < top) Arrays.fill(ptag, argsEnd, top, TAG_OBJ);
                                    frames[fp++] = new Frame(pc, prevBase, base, callableRef, closureContext);
                                    callableRef = func;
                                    code = func.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = func.getTypeMetadata().getValues();
                                    closureContext = null;
                                    pc = 0;
                                }
                            }
                            case Bytecodes.CAST -> {
                                ensureBoxed(top - 1);
                                var inst = stack[--top];
                                var type = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                if (type.isInstance(inst)) {
                                    unboxTo(top, inst);
                                    top++;
                                    pc += 3;
                                } else if (type.isAssignableFrom(inst.getValueType())) {
                                    unboxTo(top, inst);
                                    top++;
                                    pc += 3;
                                } else {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.get().getType());
                                    ExceptionNative.Exception(exception, Instances.stringInstance(
                                            String.format("Can not cast instance '%s' to type '%s'", inst.getTitle(), type.getName())
                                    ));
                                    break except;
                                }
                            }
                            case Bytecodes.CLEAR_ARRAY -> {
                                var arrayInst = stack[--top].resolveArray();
                                arrayInst.clear();
                                pc++;
                            }
                            case Bytecodes.COPY -> {
                                var sourceInst = stack[--top];
                                var copy = sourceInst.resolveMv().copy(repository::allocateRootId);
                                stack[top] = copy.getReference(); ptag[top++] = TAG_OBJ;
                                pc++;
                            }
                            case Bytecodes.INDEX_SCAN -> {
                                //noinspection DuplicatedCode
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var to = loadIndexKey(index, stack[--top]);
                                var from = loadIndexKey(index, stack[--top]);
                                var result = callContext.instanceRepository().indexScan(from, to);
                                var type = new ArrayType(index.getDeclaringType(), ArrayKind.READ_ONLY);
                                stack[top] = new ArrayInstance(type, result).getReference(); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.INDEX_COUNT -> {
                                //noinspection DuplicatedCode
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var to = loadIndexKey(index, stack[--top]);
                                var from = loadIndexKey(index, stack[--top]);
                                var count = callContext.instanceRepository().indexCount(from, to);
                                stack[top] = null; pstack[top] = count; ptag[top++] = TAG_LONG;
                                pc += 3;
                            }
                            case Bytecodes.INDEX_SELECT -> {
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var result = callContext.instanceRepository().indexSelect(loadIndexKey(index, stack[--top]));
                                var type = Types.getArrayType(index.getDeclaringType());
                                var list = Instances.createArray(type, result);
                                stack[top] = list.getReference(); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.INDEX_SELECT_FIRST -> {
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var result = callContext.instanceRepository().selectFirstByKey(loadIndexKey(index, stack[--top]));
                                stack[top] = Utils.orElse(result, new NullValue()); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.NON_NULL -> {
                                if (ptag[top - 1] != TAG_OBJ) {
                                    pc++;
                                } else if (stack[top - 1].isNull()) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("Null pointer"));
                                    break except;
                                } else
                                    pc++;
                            }
                            case Bytecodes.SET_ELEMENT -> {
                                ensureBoxed(top - 1);
                                var e = stack[--top];
                                var i = (int) pstack[--top];
                                var a = stack[--top].resolveArray();
                                a.setElement(i, a.getInstanceType().getElementType().fromStackValue(e));
                                pc++;
                            }
                            case Bytecodes.IF_EQ -> {
                                if ((int) pstack[--top] == 0)
                                    pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                else
                                    pc += 3;
                            }
                            case Bytecodes.IF_NE -> {
                                if ((int) pstack[--top] != 0)
                                    pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                else
                                    pc += 3;
                            }
                            case Bytecodes.GOTO -> pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                            case Bytecodes.INT_ADD -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 + v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_SUB -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 - v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_MUL -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 * v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_DIV -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pstack[top] = v1 / v2; ptag[top++] = TAG_INT;
                                    pc++;
                                }
                            }
                            case Bytecodes.INT_REM -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pstack[top] = v1 % v2; ptag[top++] = TAG_INT;
                                    pc++;
                                }
                            }
                            case Bytecodes.LONG_ADD -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 + v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_SUB -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 - v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_MUL -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 * v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_DIV -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pstack[top] = v1 / v2; ptag[top++] = TAG_LONG;
                                    pc++;
                                }
                            }
                            case Bytecodes.LONG_REM -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pstack[top] = v1 % v2; ptag[top++] = TAG_LONG;
                                    pc++;
                                }
                            }
                            case Bytecodes.DOUBLE_ADD -> {
                                var v2 = Double.longBitsToDouble(pstack[--top]);
                                var v1 = Double.longBitsToDouble(pstack[--top]);
                                pstack[top] = Double.doubleToRawLongBits(v1 + v2); ptag[top++] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_SUB -> {
                                var v2 = Double.longBitsToDouble(pstack[--top]);
                                var v1 = Double.longBitsToDouble(pstack[--top]);
                                pstack[top] = Double.doubleToRawLongBits(v1 - v2); ptag[top++] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_MUL -> {
                                var v2 = Double.longBitsToDouble(pstack[--top]);
                                var v1 = Double.longBitsToDouble(pstack[--top]);
                                pstack[top] = Double.doubleToRawLongBits(v1 * v2); ptag[top++] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_DIV -> {
                                var v2 = Double.longBitsToDouble(pstack[--top]);
                                var v1 = Double.longBitsToDouble(pstack[--top]);
                                pstack[top] = Double.doubleToRawLongBits(v1 / v2); ptag[top++] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_REM -> {
                                var v2 = Double.longBitsToDouble(pstack[--top]);
                                var v1 = Double.longBitsToDouble(pstack[--top]);
                                pstack[top] = Double.doubleToRawLongBits(v1 % v2); ptag[top++] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.FLOAT_ADD -> {
                                var v2 = Float.intBitsToFloat((int) pstack[--top]);
                                var v1 = Float.intBitsToFloat((int) pstack[--top]);
                                pstack[top] = Float.floatToRawIntBits(v1 + v2); ptag[top++] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_SUB -> {
                                var v2 = Float.intBitsToFloat((int) pstack[--top]);
                                var v1 = Float.intBitsToFloat((int) pstack[--top]);
                                pstack[top] = Float.floatToRawIntBits(v1 - v2); ptag[top++] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_MUL -> {
                                var v2 = Float.intBitsToFloat((int) pstack[--top]);
                                var v1 = Float.intBitsToFloat((int) pstack[--top]);
                                pstack[top] = Float.floatToRawIntBits(v1 * v2); ptag[top++] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_DIV -> {
                                var v2 = Float.intBitsToFloat((int) pstack[--top]);
                                var v1 = Float.intBitsToFloat((int) pstack[--top]);
                                pstack[top] = Float.floatToRawIntBits(v1 / v2); ptag[top++] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_REM -> {
                                var v2 = Float.intBitsToFloat((int) pstack[--top]);
                                var v1 = Float.intBitsToFloat((int) pstack[--top]);
                                pstack[top] = Float.floatToRawIntBits(v1 % v2); ptag[top++] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.INT_SHIFT_LEFT -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 << v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_SHIFT_RIGHT -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 >> v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_UNSIGNED_SHIFT_RIGHT -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 >>> v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.LONG_SHIFT_LEFT -> {
                                var v2 = (int) pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 << v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_SHIFT_RIGHT -> {
                                var v2 = (int) pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 >> v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_UNSIGNED_SHIFT_RIGHT -> {
                                var v2 = (int) pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 >>> v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.INT_BIT_OR -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 | v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_BIT_AND -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 & v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_BIT_XOR -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = v1 ^ v2; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_OR -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 | v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_AND -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 & v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_XOR -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = v1 ^ v2; ptag[top++] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.INT_NEG -> {
                                pstack[top - 1] = -(int) pstack[top - 1];
                                pc++;
                            }
                            case Bytecodes.LONG_NEG -> {
                                pstack[top - 1] = -pstack[top - 1];
                                pc++;
                            }
                            case Bytecodes.DOUBLE_NEG -> {
                                pstack[top - 1] = Double.doubleToRawLongBits(-Double.longBitsToDouble(pstack[top - 1]));
                                pc++;
                            }
                            case Bytecodes.FLOAT_NEG -> {
                                pstack[top - 1] = Float.floatToRawIntBits(-Float.intBitsToFloat((int) pstack[top - 1]));
                                pc++;
                            }
                            case Bytecodes.LONG_TO_DOUBLE -> {
                                pstack[top - 1] = Double.doubleToRawLongBits((double) pstack[top - 1]);
                                ptag[top - 1] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_LONG -> {
                                pstack[top - 1] = (long) Double.longBitsToDouble(pstack[top - 1]);
                                ptag[top - 1] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.INT_TO_LONG -> {
                                pstack[top - 1] = (int) pstack[top - 1];
                                ptag[top - 1] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.INT_TO_CHAR -> {
                                pstack[top - 1] = (char) (int) pstack[top - 1];
                                pc++;
                            }
                            case Bytecodes.INT_TO_SHORT -> {
                                pstack[top - 1] = (short) (int) pstack[top - 1];
                                pc++;
                            }
                            case Bytecodes.INT_TO_BYTE -> {
                                pstack[top - 1] = (byte) (int) pstack[top - 1];
                                pc++;
                            }
                            case Bytecodes.LONG_TO_INT -> {
                                pstack[top - 1] = (int) pstack[top - 1];
                                ptag[top - 1] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_TO_DOUBLE -> {
                                pstack[top - 1] = Double.doubleToRawLongBits((double) (int) pstack[top - 1]);
                                ptag[top - 1] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_INT -> {
                                pstack[top - 1] = (int) Double.longBitsToDouble(pstack[top - 1]);
                                ptag[top - 1] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.INT_TO_FLOAT -> {
                                pstack[top - 1] = Float.floatToRawIntBits((float) (int) pstack[top - 1]);
                                ptag[top - 1] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.LONG_TO_FLOAT -> {
                                pstack[top - 1] = Float.floatToRawIntBits((float) pstack[top - 1]);
                                ptag[top - 1] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_FLOAT -> {
                                pstack[top - 1] = Float.floatToRawIntBits((float) Double.longBitsToDouble(pstack[top - 1]));
                                ptag[top - 1] = TAG_FLOAT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_INT -> {
                                pstack[top - 1] = (int) Float.intBitsToFloat((int) pstack[top - 1]);
                                ptag[top - 1] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_LONG -> {
                                pstack[top - 1] = (long) Float.intBitsToFloat((int) pstack[top - 1]);
                                ptag[top - 1] = TAG_LONG;
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_DOUBLE -> {
                                pstack[top - 1] = Double.doubleToRawLongBits((double) Float.intBitsToFloat((int) pstack[top - 1]));
                                ptag[top - 1] = TAG_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.EQ -> {
                                pstack[top - 1] = (int) pstack[top - 1] == 0 ? 1 : 0;
                                pc++;
                            }
                            case Bytecodes.NE -> {
                                pstack[top - 1] = (int) pstack[top - 1] != 0 ? 1 : 0;
                                pc++;
                            }
                            case Bytecodes.GE -> {
                                pstack[top - 1] = (int) pstack[top - 1] >= 0 ? 1 : 0;
                                pc++;
                            }
                            case Bytecodes.GT -> {
                                pstack[top - 1] = (int) pstack[top - 1] > 0 ? 1 : 0;
                                pc++;
                            }
                            case Bytecodes.LT -> {
                                pstack[top - 1] = (int) pstack[top - 1] < 0 ? 1 : 0;
                                pc++;
                            }
                            case Bytecodes.LE -> {
                                pstack[top - 1] = (int) pstack[top - 1] <= 0 ? 1 : 0;
                                pc++;
                            }
                            case Bytecodes.INT_COMPARE -> {
                                var v2 = (int) pstack[--top];
                                var v1 = (int) pstack[--top];
                                pstack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1); ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.LONG_COMPARE -> {
                                var v2 = pstack[--top];
                                var v1 = pstack[--top];
                                pstack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1); ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_COMPARE -> {
                                var v2 = Double.longBitsToDouble(pstack[--top]);
                                var v1 = Double.longBitsToDouble(pstack[--top]);
                                pstack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1); ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_COMPARE -> {
                                var v2 = Float.intBitsToFloat((int) pstack[--top]);
                                var v1 = Float.intBitsToFloat((int) pstack[--top]);
                                pstack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1); ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.REF_COMPARE_EQ -> {
                                ensureBoxed(top - 1); ensureBoxed(top - 2);
                                var v2 = stack[--top];
                                var v1 = stack[--top];
                                stack[top] = null; pstack[top] = v1.equals(v2) ? 1 : 0; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.REF_COMPARE_NE -> {
                                ensureBoxed(top - 1); ensureBoxed(top - 2);
                                var v2 = stack[--top];
                                var v1 = stack[--top];
                                stack[top] = null; pstack[top] = !v1.equals(v2) ? 1 : 0; ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.GET_FIELD -> {
                                var i = stack[--top].resolveMvObject();
                                var p = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                unboxTo(top, i.fields[p.getRawField().offset].value.toStackValue());
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.GET_METHOD -> {
                                var i = stack[--top].resolveObject();
                                var methodRef = (MethodRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top] = i.getFunction(methodRef); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.GET_STATIC_FIELD -> {
                                var fieldRef = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var staticFieldTable = StaticFieldTable.getInstance(fieldRef.getDeclaringType(), ContextUtil.getEntityContext());
                                unboxTo(top, staticFieldTable.get(fieldRef.getRawField()).toStackValue());
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.GET_STATIC_METHOD -> {
                                var methodRef = (MethodRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top] = new FlowValue(methodRef, null); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.INSTANCE_OF -> {
                                ensureBoxed(top - 1);
                                var v = stack[--top];
                                var targetType = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top] = null; pstack[top] = targetType.isInstance(v) ? 1 : 0; ptag[top++] = TAG_INT;
                                pc += 3;
                            }
                            case Bytecodes.ARRAY_LENGTH -> {
                                var a = stack[--top].resolveArray();
                                stack[top] = null; pstack[top] = a.length(); ptag[top++] = TAG_INT;
                                pc++;
                            }
                            case Bytecodes.STORE -> {
                                var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                --top;
                                var dst = base + index;
                                stack[dst] = stack[top]; pstack[dst] = pstack[top]; ptag[dst] = ptag[top];
                                pc += 3;
                            }
                            case Bytecodes.LOAD -> {
                                var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var src = base + index;
                                stack[top] = stack[src]; pstack[top] = pstack[src]; ptag[top] = ptag[src];
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.LOAD_CONTEXT_SLOT -> {
                                var contextIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var slotIndex = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                unboxTo(top, Objects.requireNonNull(closureContext).get(contextIndex, slotIndex));
                                top++;
                                pc += 5;
                            }
                            case Bytecodes.STORE_CONTEXT_SLOT -> {
                                var contextIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var slotIndex = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                ensureBoxed(top - 1);
                                Objects.requireNonNull(closureContext).set(contextIndex, slotIndex, stack[--top]);
                                pc += 5;
                            }
                            case Bytecodes.LOAD_CONSTANT -> {
                                var value = (Value) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                unboxTo(top, value);
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.NEW_ARRAY_WITH_DIMS -> {
                                var type = (ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var array = new ArrayInstance(type);
                                var dimensions = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                var dims = new int[dimensions];
                                for (int i = dimensions - 1; i >= 0; i--) {
                                    dims[i] = (int) pstack[--top];
                                }
                                Instances.initArray(array, dims, 0);
                                stack[top] = array.getReference(); ptag[top++] = TAG_OBJ;
                                pc += 5;
                            }
                            case Bytecodes.VOID_RETURN -> {
                                if (callableRef instanceof MethodRef mr && mr.isConstructor()) {
                                    var obj = stack[base].resolveMvObject();
                                    obj.setInitialized();
                                    callContext.instanceRepository().updateMemoryIndex(obj);
                                }
                                Arrays.fill(stack, base, base + code.getFrameSize(), null);
                                Arrays.fill(ptag, base, base + code.getFrameSize(), TAG_OBJ);
                                if (fp == 0)
                                    return FlowExecResult.of(null);
                                var frame = frames[--fp];
                                frames[fp] = null;
                                pc = frame.pc;
                                top = base;
                                base = frame.base;
                                callableRef = frame.callableRef;
                                code = callableRef.getCode();
                                bytes = code.getCode();
                                constants = callableRef.getTypeMetadata().getValues();
                                closureContext = frame.closureContext;
                            }
                            case Bytecodes.DUP -> {
                                stack[top] = stack[top - 1]; pstack[top] = pstack[top - 1]; ptag[top] = ptag[top - 1];
                                top++;
                                pc++;
                            }
                            case Bytecodes.DUP2 -> {
                                stack[top] = stack[top - 2]; pstack[top] = pstack[top - 2]; ptag[top] = ptag[top - 2];
                                stack[top + 1] = stack[top - 1]; pstack[top + 1] = pstack[top - 1]; ptag[top + 1] = ptag[top - 1];
                                top += 2;
                                pc++;
                            }
                            case Bytecodes.POP -> {
                                stack[--top] = null;
                                pc++;
                            }
                            case Bytecodes.DUP_X1 -> {
                                stack[top] = stack[top - 1]; pstack[top] = pstack[top - 1]; ptag[top] = ptag[top - 1];
                                stack[top - 1] = stack[top - 2]; pstack[top - 1] = pstack[top - 2]; ptag[top - 1] = ptag[top - 2];
                                stack[top - 2] = stack[top]; pstack[top - 2] = pstack[top]; ptag[top - 2] = ptag[top];
                                top++;
                                pc++;
                            }
                            case Bytecodes.DUP_X2 -> {
                                stack[top] = stack[top - 1]; pstack[top] = pstack[top - 1]; ptag[top] = ptag[top - 1];
                                stack[top - 1] = stack[top - 2]; pstack[top - 1] = pstack[top - 2]; ptag[top - 1] = ptag[top - 2];
                                stack[top - 2] = stack[top - 3]; pstack[top - 2] = pstack[top - 3]; ptag[top - 2] = ptag[top - 3];
                                stack[top - 3] = stack[top]; pstack[top - 3] = pstack[top]; ptag[top - 3] = ptag[top];
                                top++;
                                pc++;
                            }
                            case Bytecodes.LOAD_PARENT -> {
                                var v = stack[--top];
                                var idx = (bytes[pc + 1] & 0xff) << 8 | (bytes[pc + 2] & 0xff);
                                stack[top] = requireNonNull(v.resolveMvObject().getParent(idx)).getReference(); ptag[top++] = TAG_OBJ;
                                pc += 3;
                            }
                            case Bytecodes.LOAD_CHILDREN -> {
                                var v = stack[--top];
                                stack[top] = Instances.arrayValue(Utils.map(v.resolveMvObject().getChildren(), Instance::getReference)); ptag[top++] = TAG_OBJ;
                                pc++;
                            }
                            case Bytecodes.ID -> {
                                var v = stack[--top];
                                stack[top] = Instances.stringInstance(v.resolveMvObject().getStringId()); ptag[top++] = TAG_OBJ;
                                pc++;
                            }
                            case Bytecodes.TABLE_SWITCH -> {
                                var k = (int) pstack[--top];
                                int p = pc + 4 & 0xfffffffc;
                                int defaultOffset = (bytes[p] & 0xff) << 24 | (bytes[p + 1] & 0xff) << 16
                                        | (bytes[p + 2] & 0xff) << 8 | bytes[p + 3] & 0xff;
                                int low = (bytes[p + 4] & 0xff) << 24 | (bytes[p + 5] & 0xff) << 16
                                        | (bytes[p + 6] & 0xff) << 8 | bytes[p + 7] & 0xff;
                                int high = (bytes[p + 8] & 0xff) << 24 | (bytes[p + 9] & 0xff) << 16
                                        | (bytes[p + 10] & 0xff) << 8 | bytes[p + 11] & 0xff;
                                int offset;
                                if (k < low || k > high) {
                                    offset = defaultOffset;
                                } else {
                                    p = p + 12 + (k - low << 2);
                                    offset = (bytes[p] & 0xff) << 24 | (bytes[p + 1] & 0xff) << 16
                                            | (bytes[p + 2] & 0xff) << 8 | bytes[p + 3] & 0xff;
                                }
                                pc += offset;
                            }
                            case Bytecodes.LOOKUP_SWITCH -> {
                                var k = (int) pstack[--top];
                                int p = pc + 4 & 0xfffffffc;
                                int offset = (bytes[p] & 0xff) << 24 | (bytes[p + 1] & 0xff) << 16
                                        | (bytes[p + 2] & 0xff) << 8 | bytes[p + 3] & 0xff;
                                int l = 0;
                                int h = (bytes[p + 4] & 0xff) << 24 | (bytes[p + 5] & 0xff) << 16
                                        | (bytes[p + 6] & 0xff) << 8 | bytes[p + 7] & 0xff;
                                p += 8;
                                while (l < h) {
                                    int m = l + h >> 1;
                                    int p1 = p + (m << 3);
                                    int match = (bytes[p1] & 0xff) << 24 | (bytes[p1 + 1] & 0xff) << 16
                                            | (bytes[p1 + 2] & 0xff) << 8 | bytes[p1 + 3] & 0xff;
                                    if (k == match) {
                                        offset = (bytes[p1 + 4] & 0xff) << 24 | (bytes[p1 + 5] & 0xff) << 16
                                                | (bytes[p1 + 6] & 0xff) << 8 | bytes[p1 + 7] & 0xff;
                                        break;
                                    } else if (k < match)
                                        h = m;
                                    else
                                        l = m + 1;
                                }
                                pc += offset;
                            }
                            case Bytecodes.DELETE -> {
                                repository.remove(stack[--top].resolveObject());
                                pc++;
                            }
                            case Bytecodes.SET_FIELD_REFRESH -> {
                                int fieldIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var field = (FieldRef) constants[fieldIndex];
                                ensureBoxed(top - 1);
                                var value = stack[--top];
                                var instance = stack[--top].resolveMvObject();
                                instance.fields[field.getRawField().offset].value = field.getPropertyType().fromStackValue(value);
                                if (instance.isInitialized())
                                    callContext.instanceRepository().updateMemoryIndex(instance);
                                pc += 3;
                            }
                            default -> throw new IllegalStateException("Invalid bytecode: " + b);
                        }
                        continue;
                    }

                    assert exception != null;
                    if(handlerTop > 0) {
                        var h = exceptionHandlers[--handlerTop];
                        if (h.fp == fp)
                            pc = h.pc;
                        else {
                            var f = frames[h.fp];
                            int cleanFrom = f.base + f.callableRef.getCode().getFrameSize();
                            int cleanTo = base + code.getFrameSize();
                            Arrays.fill(stack, cleanFrom, cleanTo, null);
                            Arrays.fill(ptag, cleanFrom, cleanTo, TAG_OBJ);
                            fp = h.fp;
                            base = f.base;
                            top = f.top;
                            callableRef = f.callableRef;
                            code = callableRef.getCode();
                            bytes = code.getCode();
                            pc = h.pc;
                            constants = callableRef.getTypeMetadata().getValues();
                            closureContext = f.closureContext;
                        }
                        stack[top] = exception.getReference(); ptag[top++] = TAG_OBJ;
                    }
                    else {
                        Arrays.fill(stack, 0, base + code.getFrameSize(), null);
                        Arrays.fill(ptag, 0, base + code.getFrameSize(), TAG_OBJ);
                        return FlowExecResult.ofException(exception);
                    }

                }
                catch (BusinessException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new InternalException("Failed to execute node " + Bytecodes.getBytecodeName(b) +  " at " + pc
                            + " in flow " + code.getFlow().getQualifiedName(), e);
                }
            }
        } finally {
//            if(DebugEnv.flag)
//                log.debug("Exiting flow {}", scope.getFlow().getQualifiedName());
        }
    }


    private FlowRef tryUncaptureFlow(FlowRef flow, int[] capturedVariableIndexes, Type[] capturedVariableTypes, Value[] stack, int base) {
        if(capturedVariableIndexes.length == 0)
            return flow;
        var actualExprTypes = new Type[capturedVariableIndexes.length];
        for (int i = 0; i < capturedVariableIndexes.length; i++) {
            actualExprTypes[i] = stack[base + capturedVariableIndexes[i]].getValueType();
        }
        var capturedTypeMap = new HashMap<CapturedType, Type>();
        for (int i = 0; i < actualExprTypes.length; i++) {
            var capturedType = capturedVariableTypes[i];
            Types.extractCapturedType(capturedType, actualExprTypes[i], capturedTypeMap::put);
        }
        // TODO Create a constructor in TypeSubstitutor that accepts a Map
        var capturedTypes = new ArrayList<CapturedType>();
        var actualCapturedTypes = new ArrayList<Type>();
        capturedTypeMap.forEach((ct, t) -> {
            capturedTypes.add(ct);
            actualCapturedTypes.add(t);
        });
        var typeSubst = new TypeSubstitutor(capturedTypes, actualCapturedTypes);
        if(flow instanceof MethodRef method && method.getDeclaringType().isParameterized()
                && Utils.anyMatch(method.getDeclaringType().getTypeArguments(), Type::isCaptured)) {
            var declaringType = method.getDeclaringType();
            var actualTypeArgs = Utils.map(declaringType.getTypeArguments(), t -> t.accept(typeSubst));
            var actualDeclaringType = new KlassType(declaringType.getOwner(), declaringType.getKlass(), actualTypeArgs);
            if(DebugEnv.debugging)
                log.info("uncapture flow declaring type from {} to {}",
                        declaringType.getTypeDesc(),
                        actualDeclaringType.getTypeDesc());
            flow = Objects.requireNonNull(actualDeclaringType.findSelfMethod(m -> m.getRawFlow() == method.getRawFlow()));
        }
        if(Utils.anyMatch(flow.getTypeArguments(), Type::isCaptured)) {
            var actualTypeArgs = Utils.map(flow.getTypeArguments(), t -> t.accept(typeSubst));
            return flow.getParameterized(actualTypeArgs);
        }
        else
            return flow;
    }

    public IndexKeyRT loadIndexKey(IndexRef indexRef, Value key) {
        var values = Indexes.getIndexValues(indexRef, key);
        var index = indexRef.getRawIndex();
        return index.createIndexKey(values);
    }

    private record Frame(
        int pc,
        int base,
        int top,
        CallableRef callableRef,
        ClosureContext closureContext) {
    }

    private record ExceptionHandler(int fp, int pc) {}

}
