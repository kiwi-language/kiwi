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
import static org.manul.object.instance.core.IntValue.one;
import static org.manul.object.instance.core.IntValue.zero;

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

    // Type tags for the primitive stack
    private static final byte T_REF = 0;
    private static final byte T_INT = 1;
    private static final byte T_LONG = 2;
    private static final byte T_DOUBLE = 3;
    private static final byte T_FLOAT = 4;

    private final ExceptionHandler[] exceptionHandlers = new ExceptionHandler[1024];
    private final Value[] stack = new Value[1024 * 1024];
    private final long[] pStack = new long[1024 * 1024];
    private final byte[] tStack = new byte[1024 * 1024];
    private final Frame[] frames = new Frame[1024];

    private VmStack() {
    }

    /**
     * Materialize a Value from the dual stack at the given position.
     * For reference slots, returns the existing Value from stack[].
     * For primitive slots, creates a new Value from pStack[].
     */
    private Value ensureValue(int pos) {
        return switch (tStack[pos]) {
            case T_REF -> stack[pos];
            case T_INT -> IntValue.of((int) pStack[pos]);
            case T_LONG -> new LongValue(pStack[pos]);
            case T_DOUBLE -> new DoubleValue(Double.longBitsToDouble(pStack[pos]));
            case T_FLOAT -> new FloatValue(Float.intBitsToFloat((int) pStack[pos]));
            default -> throw new IllegalStateException("Invalid type tag: " + tStack[pos]);
        };
    }

    /**
     * Push a Value (from an external source) onto both stacks at the given position.
     * Extracts raw primitive bits into pStack and sets the type tag.
     */
    private void pushValue(int pos, Value v) {
        stack[pos] = v;
        if (v instanceof IntValue iv) {
            pStack[pos] = iv.value;
            tStack[pos] = T_INT;
        } else if (v instanceof LongValue lv) {
            pStack[pos] = lv.value;
            tStack[pos] = T_LONG;
        } else if (v instanceof DoubleValue dv) {
            pStack[pos] = Double.doubleToRawLongBits(dv.value);
            tStack[pos] = T_DOUBLE;
        } else if (v instanceof FloatValue fv) {
            pStack[pos] = Float.floatToRawIntBits(fv.value);
            tStack[pos] = T_FLOAT;
        } else {
            tStack[pos] = T_REF;
        }
    }

    /**
     * Ensure all primitive slots in [from, to) have a materialized Value in stack[].
     * Used before capturing stack slices into ClosureContext.
     */
    private void materializeRange(int from, int to) {
        for (int i = from; i < to; i++) {
            if (tStack[i] != T_REF) {
                stack[i] = ensureValue(i);
            }
        }
    }

    @SuppressWarnings({"DuplicatedCode", "UseCompareMethod", "DataFlowIssue", "ExtractMethodRecommender"})
    private @NotNull FlowExecResult execute0(CallableRef callableRef,
                                           Value[] arguments,
                                           @Nullable ClosureContext closureContext,
                                           CallContext callContext) {

        try {
            var constants = callableRef.getTypeMetadata().getValues();
            // Initialize arguments into both stacks
            for (int i = 0; i < arguments.length; i++) {
                pushValue(i, arguments[i]);
            }
            var stack = this.stack;
            var pStack = this.pStack;
            var tStack = this.tStack;
            var frames = this.frames;
            int base = 0;
            var code = callableRef.getCode();
            int top = code.getMaxLocals();
            int handlerTop = 0;
            int pc = 0;
            var bytes = code.getCode();
            var repository = Utils.safeCall(callContext, CallContext::instanceRepository);
            var fp = 0;
            ClassInstance exception;

            for (;;) {
                var b = bytes[pc] & 0xff;
                try {
                    except: {
                        switch (b) {
                            case Bytecodes.ADD_OBJECT -> {
                                int typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                boolean ephemeral = bytes[pc + 3] == 1;
                                var instance = ClassInstanceBuilder.newBuilder(type, repository.allocateRootId(type))
                                        .ephemeral(ephemeral)
                                        .build();
                                var fields = type.getKlass().getAllFields();
                                int numFields = fields.size();
                                var fieldValues = new Value[numFields];
                                for (int i = numFields - 1; i >= 0; i--) {
                                    fieldValues[i] = ensureValue(--top);
                                }
                                var fieldIt = fields.iterator();
                                for (int i = 0; i < numFields; i++) {
                                    var f = fieldIt.next();
                                    instance.initField(f, f.getType().fromStackValue(fieldValues[i]));
                                }
                                if (!instance.isEphemeral())
                                    callContext.instanceRepository().bind(instance);
                                stack[top] = instance.getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 4;
                            }
                            case Bytecodes.SET_FIELD -> {
                                int fieldIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var field = (FieldRef) constants[fieldIndex];
                                var value = ensureValue(--top);
                                var instance = stack[--top].resolveMvObject();
                                instance.fields[field.getRawField().offset].value = field.getPropertyType().fromStackValue(value);
                                pc += 3;
                            }
                            case Bytecodes.RETURN -> {
                                // Save return value before cleanup
                                int retSlot = top - 1;
                                long retPrim = pStack[retSlot];
                                byte retTag = tStack[retSlot];
                                Value retRef = stack[retSlot];

                                Arrays.fill(stack, base, base + code.getFrameSize(), null);

                                if (fp == 0) {
                                    Value v = retTag != T_REF ? ensureValue(retSlot) : retRef;
                                    return FlowExecResult.of(v);
                                }
                                var frame = frames[--fp];
                                frames[fp] = null;

                                // Place return value at caller's expected position
                                stack[frame.top] = retTag != T_REF ? ensureValue(retSlot) : retRef;
                                pStack[frame.top] = retPrim;
                                tStack[frame.top] = retTag;

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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    var r = NativeMethods.invoke(method.getRawFlow(), null, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount();
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    var r = NativeMethods.invoke(method.getRawFlow(), null, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount();
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
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
                                Value result = callContext.instanceRepository().selectFirstByKey(loadIndexKey(index, ensureValue(--top)));
                                if (result == null)
                                    result = NullValue.instance;
                                pushValue(top, result);
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.NEW -> {
                                var typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                var ephemeral = bytes[pc + 3] == 1;
                                var unbound = bytes[pc + 4] == 1;
                                if (type.isLocal()) materializeRange(base, top);
                                var self = ClassInstanceBuilder.newBuilder(type, repository.allocateRootId(type))
                                        .ephemeral(ephemeral)
                                        .closureContext(type.isLocal() ? new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)) : null)
                                        .build();
                                if (!self.isEphemeral() && !unbound)
                                    callContext.instanceRepository().bind(self);
                                stack[top] = self.getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 5;
                            }
                            case Bytecodes.NEW_CHILD -> {
                                var typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                var parent = stack[--top].resolveObject();
                                if (type.isLocal()) materializeRange(base, top);
                                var self = ClassInstanceBuilder.newBuilder(type, parent.getRoot().nextChildId())
                                        .parent(parent)
                                        .closureContext(type.isLocal() ? new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top + 1)) : null)
                                        .build();
                                stack[top] = self.getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.SET_STATIC -> {
                                var field = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var sft = StaticFieldTable.getInstance(field.getDeclaringType(), ContextUtil.getEntityContext());
                                sft.set(field.getRawField(), ensureValue(--top));
                                pc += 3;
                            }
                            case Bytecodes.NEW_ARRAY -> {
                                var array = new ArrayInstance((ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff]);
                                stack[top] = array.getReference();
                                tStack[top] = T_REF;
                                top++;
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
                                int prevTop = top - functionType.getParameterTypes().size() - 1;
                                var funcInst = (FunctionValue) stack[prevTop];
                                var self = funcInst.getSelf();
                                if (self != null)
                                    stack[base = prevTop] = self;
                                else
                                    base = prevTop + 1;
                                top = base + funcInst.getCode().getMaxLocals();
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
                                materializeRange(base, top);
                                var func = new LambdaValue(lambda, new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)));
                                if (bytes[pc + 3] == 0) {
                                    stack[top] = func;
                                    tStack[top] = T_REF;
                                    top++;
                                    pc += 4;
                                } else {
                                    var functionalInterface = (ClassType) constants[bytes[pc + 4] | bytes[pc + 5]];
                                    var functionInterfaceImpl = Types.createFunctionalClass(functionalInterface);
                                    var funcImplKlass = functionInterfaceImpl.getKlass();
                                    var funcField = funcImplKlass.getFieldByName("func");
                                    stack[top] = ClassInstance.create(TmpId.random(), Map.of(funcField, func), functionInterfaceImpl).getReference();
                                    tStack[top] = T_REF;
                                    top++;
                                    pc += 6;
                                }
                            }
                            case Bytecodes.ADD_ELEMENT -> {
                                var e = ensureValue(--top);
                                var a = stack[--top].resolveArray();
                                a.addElement(a.getInstanceType().getElementType().fromStackValue(e));
                                pc++;
                            }
                            case Bytecodes.DELETE_ELEMENT -> {
                                var elem = ensureValue(--top);
                                var array = stack[--top].resolveArray();
                                var r = array.remove(elem);
                                pStack[top] = r ? 1 : 0;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.GET_ELEMENT -> {
                                var index = (int) pStack[--top];
                                var arrayInst = stack[--top].resolveArray();
                                if (index < arrayInst.size()) {
                                    var elem = arrayInst.get(index).toStackValue();
                                    pushValue(top, elem);
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    var nativeCode = Objects.requireNonNull(func.getRawFlow().getNativeCode());
                                    var r = nativeCode.run(func, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!func.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - func.getParameterCount();
                                    top = base + func.getRawFlow().getCode().getMaxLocals();
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
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = ensureValue(--top);
                                    }
                                    var nativeCode = Objects.requireNonNull(func.getRawFlow().getNativeCode());
                                    var r = nativeCode.run(func, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!func.getReturnType().isVoid()) {
                                        pushValue(top, r.ret());
                                        top++;
                                    }
                                } else {
                                    int prevBase = base;
                                    base = top - func.getParameterCount();
                                    top = base + func.getRawFlow().getCode().getMaxLocals();
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
                                var inst = ensureValue(--top);
                                var type = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                if (type.isInstance(inst)) {
                                    pushValue(top, inst);
                                    top++;
                                    pc += 3;
                                } else if (type.isAssignableFrom(inst.getValueType())) {
                                    pushValue(top, inst);
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
                                stack[top] = copy.getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INDEX_SCAN -> {
                                //noinspection DuplicatedCode
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var to = loadIndexKey(index, ensureValue(--top));
                                var from = loadIndexKey(index, ensureValue(--top));
                                var result = callContext.instanceRepository().indexScan(from, to);
                                var type = new ArrayType(index.getDeclaringType(), ArrayKind.READ_ONLY);
                                stack[top] = new ArrayInstance(type, result).getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.INDEX_COUNT -> {
                                //noinspection DuplicatedCode
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var to = loadIndexKey(index, ensureValue(--top));
                                var from = loadIndexKey(index, ensureValue(--top));
                                var count = callContext.instanceRepository().indexCount(from, to);
                                pStack[top] = count;
                                tStack[top] = T_LONG;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.INDEX_SELECT -> {
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var result = callContext.instanceRepository().indexSelect(loadIndexKey(index, ensureValue(--top)));
                                var type = Types.getArrayType(index.getDeclaringType());
                                var list = Instances.createArray(type, result);
                                stack[top] = list.getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.INDEX_SELECT_FIRST -> {
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var result = callContext.instanceRepository().selectFirstByKey(loadIndexKey(index, ensureValue(--top)));
                                var v = Utils.orElse(result, NullValue.instance);
                                pushValue(top, v);
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.NON_NULL -> {
                                var inst = ensureValue(top - 1);
                                if (inst.isNull()) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("Null pointer"));
                                    break except;
                                } else
                                    pc++;
                            }
                            case Bytecodes.SET_ELEMENT -> {
                                var e = ensureValue(--top);
                                var i = (int) pStack[--top];
                                var a = stack[--top].resolveArray();
                                a.setElement(i, a.getInstanceType().getElementType().fromStackValue(e));
                                pc++;
                            }

                            // --- Control flow: read from pStack ---
                            case Bytecodes.IF_EQ -> {
                                if ((int) pStack[--top] == 0)
                                    pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                else
                                    pc += 3;
                            }
                            case Bytecodes.IF_NE -> {
                                if ((int) pStack[--top] != 0)
                                    pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                else
                                    pc += 3;
                            }
                            case Bytecodes.GOTO -> pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);

                            // --- Integer arithmetic: zero allocation, pStack only ---
                            case Bytecodes.INT_ADD -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 + v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_SUB -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 - v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_MUL -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 * v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_DIV -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pStack[top] = v1 / v2;
                                    tStack[top] = T_INT;
                                    top++;
                                    pc++;
                                }
                            }
                            case Bytecodes.INT_REM -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pStack[top] = v1 % v2;
                                    tStack[top] = T_INT;
                                    top++;
                                    pc++;
                                }
                            }

                            // --- Long arithmetic: zero allocation, pStack only ---
                            case Bytecodes.LONG_ADD -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 + v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_SUB -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 - v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_MUL -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 * v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_DIV -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pStack[top] = v1 / v2;
                                    tStack[top] = T_LONG;
                                    top++;
                                    pc++;
                                }
                            }
                            case Bytecodes.LONG_REM -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                                    ExceptionNative.Exception(exception, Instances.stringInstance("/ by zero"));
                                    break except;
                                } else {
                                    pStack[top] = v1 % v2;
                                    tStack[top] = T_LONG;
                                    top++;
                                    pc++;
                                }
                            }

                            // --- Double arithmetic: zero allocation, pStack only ---
                            case Bytecodes.DOUBLE_ADD -> {
                                double v2 = Double.longBitsToDouble(pStack[--top]);
                                double v1 = Double.longBitsToDouble(pStack[--top]);
                                pStack[top] = Double.doubleToRawLongBits(v1 + v2);
                                tStack[top] = T_DOUBLE;
                                top++;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_SUB -> {
                                double v2 = Double.longBitsToDouble(pStack[--top]);
                                double v1 = Double.longBitsToDouble(pStack[--top]);
                                pStack[top] = Double.doubleToRawLongBits(v1 - v2);
                                tStack[top] = T_DOUBLE;
                                top++;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_MUL -> {
                                double v2 = Double.longBitsToDouble(pStack[--top]);
                                double v1 = Double.longBitsToDouble(pStack[--top]);
                                pStack[top] = Double.doubleToRawLongBits(v1 * v2);
                                tStack[top] = T_DOUBLE;
                                top++;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_DIV -> {
                                double v2 = Double.longBitsToDouble(pStack[--top]);
                                double v1 = Double.longBitsToDouble(pStack[--top]);
                                pStack[top] = Double.doubleToRawLongBits(v1 / v2);
                                tStack[top] = T_DOUBLE;
                                top++;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_REM -> {
                                double v2 = Double.longBitsToDouble(pStack[--top]);
                                double v1 = Double.longBitsToDouble(pStack[--top]);
                                pStack[top] = Double.doubleToRawLongBits(v1 % v2);
                                tStack[top] = T_DOUBLE;
                                top++;
                                pc++;
                            }

                            // --- Float arithmetic: zero allocation, pStack only ---
                            case Bytecodes.FLOAT_ADD -> {
                                float v2 = Float.intBitsToFloat((int) pStack[--top]);
                                float v1 = Float.intBitsToFloat((int) pStack[--top]);
                                pStack[top] = Float.floatToRawIntBits(v1 + v2);
                                tStack[top] = T_FLOAT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.FLOAT_SUB -> {
                                float v2 = Float.intBitsToFloat((int) pStack[--top]);
                                float v1 = Float.intBitsToFloat((int) pStack[--top]);
                                pStack[top] = Float.floatToRawIntBits(v1 - v2);
                                tStack[top] = T_FLOAT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.FLOAT_MUL -> {
                                float v2 = Float.intBitsToFloat((int) pStack[--top]);
                                float v1 = Float.intBitsToFloat((int) pStack[--top]);
                                pStack[top] = Float.floatToRawIntBits(v1 * v2);
                                tStack[top] = T_FLOAT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.FLOAT_DIV -> {
                                float v2 = Float.intBitsToFloat((int) pStack[--top]);
                                float v1 = Float.intBitsToFloat((int) pStack[--top]);
                                pStack[top] = Float.floatToRawIntBits(v1 / v2);
                                tStack[top] = T_FLOAT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.FLOAT_REM -> {
                                float v2 = Float.intBitsToFloat((int) pStack[--top]);
                                float v1 = Float.intBitsToFloat((int) pStack[--top]);
                                pStack[top] = Float.floatToRawIntBits(v1 % v2);
                                tStack[top] = T_FLOAT;
                                top++;
                                pc++;
                            }

                            // --- Shift and bitwise: zero allocation, pStack only ---
                            case Bytecodes.INT_SHIFT_LEFT -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 << v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_SHIFT_RIGHT -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 >> v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_UNSIGNED_SHIFT_RIGHT -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 >>> v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_SHIFT_LEFT -> {
                                int v2 = (int) pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 << v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_SHIFT_RIGHT -> {
                                int v2 = (int) pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 >> v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_UNSIGNED_SHIFT_RIGHT -> {
                                int v2 = (int) pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 >>> v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_BIT_OR -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 | v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_BIT_AND -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 & v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.INT_BIT_XOR -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = v1 ^ v2;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_OR -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 | v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_AND -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 & v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_XOR -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = v1 ^ v2;
                                tStack[top] = T_LONG;
                                top++;
                                pc++;
                            }

                            // --- Negation: zero allocation, pStack only ---
                            case Bytecodes.INT_NEG -> {
                                pStack[top - 1] = -((int) pStack[top - 1]);
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.LONG_NEG -> {
                                pStack[top - 1] = -pStack[top - 1];
                                tStack[top - 1] = T_LONG;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_NEG -> {
                                pStack[top - 1] = Double.doubleToRawLongBits(-Double.longBitsToDouble(pStack[top - 1]));
                                tStack[top - 1] = T_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.FLOAT_NEG -> {
                                pStack[top - 1] = Float.floatToRawIntBits(-Float.intBitsToFloat((int) pStack[top - 1]));
                                tStack[top - 1] = T_FLOAT;
                                pc++;
                            }

                            // --- Type conversions: zero allocation, pStack only ---
                            case Bytecodes.INT_TO_LONG -> {
                                // pStack already holds the widened int as long
                                tStack[top - 1] = T_LONG;
                                pc++;
                            }
                            case Bytecodes.INT_TO_DOUBLE -> {
                                pStack[top - 1] = Double.doubleToRawLongBits((double) (int) pStack[top - 1]);
                                tStack[top - 1] = T_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.INT_TO_FLOAT -> {
                                pStack[top - 1] = Float.floatToRawIntBits((float) (int) pStack[top - 1]);
                                tStack[top - 1] = T_FLOAT;
                                pc++;
                            }
                            case Bytecodes.INT_TO_CHAR -> {
                                pStack[top - 1] = (char) (int) pStack[top - 1];
                                pc++;
                            }
                            case Bytecodes.INT_TO_SHORT -> {
                                pStack[top - 1] = (short) (int) pStack[top - 1];
                                pc++;
                            }
                            case Bytecodes.INT_TO_BYTE -> {
                                pStack[top - 1] = (byte) (int) pStack[top - 1];
                                pc++;
                            }
                            case Bytecodes.LONG_TO_INT -> {
                                pStack[top - 1] = (int) pStack[top - 1];
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.LONG_TO_DOUBLE -> {
                                pStack[top - 1] = Double.doubleToRawLongBits((double) pStack[top - 1]);
                                tStack[top - 1] = T_DOUBLE;
                                pc++;
                            }
                            case Bytecodes.LONG_TO_FLOAT -> {
                                pStack[top - 1] = Float.floatToRawIntBits((float) pStack[top - 1]);
                                tStack[top - 1] = T_FLOAT;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_INT -> {
                                pStack[top - 1] = (int) Double.longBitsToDouble(pStack[top - 1]);
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_LONG -> {
                                pStack[top - 1] = (long) Double.longBitsToDouble(pStack[top - 1]);
                                tStack[top - 1] = T_LONG;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_FLOAT -> {
                                pStack[top - 1] = Float.floatToRawIntBits((float) Double.longBitsToDouble(pStack[top - 1]));
                                tStack[top - 1] = T_FLOAT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_INT -> {
                                pStack[top - 1] = (int) Float.intBitsToFloat((int) pStack[top - 1]);
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_LONG -> {
                                pStack[top - 1] = (long) Float.intBitsToFloat((int) pStack[top - 1]);
                                tStack[top - 1] = T_LONG;
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_DOUBLE -> {
                                pStack[top - 1] = Double.doubleToRawLongBits((double) Float.intBitsToFloat((int) pStack[top - 1]));
                                tStack[top - 1] = T_DOUBLE;
                                pc++;
                            }

                            // --- Comparisons on int result: zero allocation, pStack only ---
                            case Bytecodes.EQ -> {
                                pStack[top - 1] = (int) pStack[top - 1] == 0 ? 1 : 0;
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.NE -> {
                                pStack[top - 1] = (int) pStack[top - 1] != 0 ? 1 : 0;
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.GE -> {
                                pStack[top - 1] = (int) pStack[top - 1] >= 0 ? 1 : 0;
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.GT -> {
                                pStack[top - 1] = (int) pStack[top - 1] > 0 ? 1 : 0;
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.LT -> {
                                pStack[top - 1] = (int) pStack[top - 1] < 0 ? 1 : 0;
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.LE -> {
                                pStack[top - 1] = (int) pStack[top - 1] <= 0 ? 1 : 0;
                                tStack[top - 1] = T_INT;
                                pc++;
                            }
                            case Bytecodes.INT_COMPARE -> {
                                int v2 = (int) pStack[--top];
                                int v1 = (int) pStack[--top];
                                pStack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LONG_COMPARE -> {
                                long v2 = pStack[--top];
                                long v1 = pStack[--top];
                                pStack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.DOUBLE_COMPARE -> {
                                double v2 = Double.longBitsToDouble(pStack[--top]);
                                double v1 = Double.longBitsToDouble(pStack[--top]);
                                pStack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.FLOAT_COMPARE -> {
                                float v2 = Float.intBitsToFloat((int) pStack[--top]);
                                float v1 = Float.intBitsToFloat((int) pStack[--top]);
                                pStack[top] = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.REF_COMPARE_EQ -> {
                                var v2 = ensureValue(--top);
                                var v1 = ensureValue(--top);
                                pStack[top] = v1.equals(v2) ? 1 : 0;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }
                            case Bytecodes.REF_COMPARE_NE -> {
                                var v2 = ensureValue(--top);
                                var v1 = ensureValue(--top);
                                pStack[top] = !v1.equals(v2) ? 1 : 0;
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }

                            // --- Field access ---
                            case Bytecodes.GET_FIELD -> {
                                var i = stack[--top].resolveMvObject();
                                var p = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var fieldValue = i.fields[p.getRawField().offset].value.toStackValue();
                                pushValue(top, fieldValue);
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.GET_METHOD -> {
                                var i = stack[--top].resolveObject();
                                var methodRef = (MethodRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top] = i.getFunction(methodRef);
                                tStack[top] = T_REF;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.GET_STATIC_FIELD -> {
                                var fieldRef = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var staticFieldTable = StaticFieldTable.getInstance(fieldRef.getDeclaringType(), ContextUtil.getEntityContext());
                                var fieldValue = staticFieldTable.get(fieldRef.getRawField()).toStackValue();
                                pushValue(top, fieldValue);
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.GET_STATIC_METHOD -> {
                                var methodRef = (MethodRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top] = new FlowValue(methodRef, null);
                                tStack[top] = T_REF;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.INSTANCE_OF -> {
                                var v = ensureValue(--top);
                                var targetType = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                pStack[top] = targetType.isInstance(v) ? 1 : 0;
                                tStack[top] = T_INT;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.ARRAY_LENGTH -> {
                                var a = stack[--top].resolveArray();
                                pStack[top] = a.length();
                                tStack[top] = T_INT;
                                top++;
                                pc++;
                            }

                            // --- Local variable access: mirror both stacks ---
                            case Bytecodes.STORE -> {
                                var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                --top;
                                int slot = base + index;
                                stack[slot] = stack[top];
                                pStack[slot] = pStack[top];
                                tStack[slot] = tStack[top];
                                pc += 3;
                            }
                            case Bytecodes.LOAD -> {
                                var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                int slot = base + index;
                                stack[top] = stack[slot];
                                pStack[top] = pStack[slot];
                                tStack[top] = tStack[slot];
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.LOAD_CONTEXT_SLOT -> {
                                var contextIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var slotIndex = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                var v = Objects.requireNonNull(closureContext).get(contextIndex, slotIndex);
                                pushValue(top, v);
                                top++;
                                pc += 5;
                            }
                            case Bytecodes.STORE_CONTEXT_SLOT -> {
                                var contextIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var slotIndex = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                Objects.requireNonNull(closureContext).set(contextIndex, slotIndex, ensureValue(--top));
                                pc += 5;
                            }
                            case Bytecodes.LOAD_CONSTANT -> {
                                var value = (Value) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                pushValue(top, value);
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.NEW_ARRAY_WITH_DIMS -> {
                                var type = (ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var array = new ArrayInstance(type);
                                var dimensions = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                var dims = new int[dimensions];
                                for (int i = dimensions - 1; i >= 0; i--) {
                                    dims[i] = (int) pStack[--top];
                                }
                                Instances.initArray(array, dims, 0);
                                stack[top] = array.getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 5;
                            }
                            case Bytecodes.VOID_RETURN -> {
                                if (callableRef instanceof MethodRef mr && mr.isConstructor()) {
                                    var obj = stack[base].resolveMvObject();
                                    obj.setInitialized();
                                    callContext.instanceRepository().updateMemoryIndex(obj);
                                }
                                Arrays.fill(stack, base, base + code.getFrameSize(), null);
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

                            // --- Stack manipulation: mirror both stacks ---
                            case Bytecodes.DUP -> {
                                stack[top] = stack[top - 1];
                                pStack[top] = pStack[top - 1];
                                tStack[top] = tStack[top - 1];
                                top++;
                                pc++;
                            }
                            case Bytecodes.DUP2 -> {
                                int d2a = top - 2;
                                int d2b = top - 1;
                                stack[top] = stack[d2a];
                                pStack[top] = pStack[d2a];
                                tStack[top] = tStack[d2a];
                                top++;
                                stack[top] = stack[d2b];
                                pStack[top] = pStack[d2b];
                                tStack[top] = tStack[d2b];
                                top++;
                                pc++;
                            }
                            case Bytecodes.POP -> {
                                stack[--top] = null;
                                pc++;
                            }
                            case Bytecodes.DUP_X1 -> {
                                // Copy top element
                                stack[top] = stack[top - 1];
                                pStack[top] = pStack[top - 1];
                                tStack[top] = tStack[top - 1];
                                // Move second-from-top up
                                stack[top - 1] = stack[top - 2];
                                pStack[top - 1] = pStack[top - 2];
                                tStack[top - 1] = tStack[top - 2];
                                // Insert copy at third position
                                stack[top - 2] = stack[top];
                                pStack[top - 2] = pStack[top];
                                tStack[top - 2] = tStack[top];
                                top++;
                                pc++;
                            }
                            case Bytecodes.DUP_X2 -> {
                                // Copy top element
                                stack[top] = stack[top - 1];
                                pStack[top] = pStack[top - 1];
                                tStack[top] = tStack[top - 1];
                                // Shift down
                                stack[top - 1] = stack[top - 2];
                                pStack[top - 1] = pStack[top - 2];
                                tStack[top - 1] = tStack[top - 2];
                                stack[top - 2] = stack[top - 3];
                                pStack[top - 2] = pStack[top - 3];
                                tStack[top - 2] = tStack[top - 3];
                                // Insert copy at fourth position
                                stack[top - 3] = stack[top];
                                pStack[top - 3] = pStack[top];
                                tStack[top - 3] = tStack[top];
                                top++;
                                pc++;
                            }
                            case Bytecodes.LOAD_PARENT -> {
                                var v = stack[--top];
                                var idx = (bytes[pc + 1] & 0xff) << 8 | (bytes[pc + 2] & 0xff);
                                stack[top] = requireNonNull(v.resolveMvObject().getParent(idx)).getReference();
                                tStack[top] = T_REF;
                                top++;
                                pc += 3;
                            }
                            case Bytecodes.LOAD_CHILDREN -> {
                                var v = stack[--top];
                                stack[top] = Instances.arrayValue(Utils.map(v.resolveMvObject().getChildren(), Instance::getReference));
                                tStack[top] = T_REF;
                                top++;
                                pc++;
                            }
                            case Bytecodes.ID -> {
                                var v = stack[--top];
                                stack[top] = Instances.stringInstance(v.resolveMvObject().getStringId());
                                tStack[top] = T_REF;
                                top++;
                                pc++;
                            }
                            case Bytecodes.TABLE_SWITCH -> {
                                int k = (int) pStack[--top];
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
                                int k = (int) pStack[--top];
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
                                var value = ensureValue(--top);
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
                            Arrays.fill(stack, f.base + f.callableRef.getCode().getFrameSize(), base + code.getFrameSize(), null);
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
                        stack[top] = exception.getReference();
                        tStack[top] = T_REF;
                        top++;
                    }
                    else {
                        Arrays.fill(stack, 0, base + code.getFrameSize(), null);
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
