package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.*;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.metavm.object.instance.core.IntValue.one;
import static org.metavm.object.instance.core.IntValue.zero;

@Slf4j
public class VmStack {

    private static final ObjectPool<VmStack> pool = new ObjectPool<>(1024, VmStack::new);

    public static FlowExecResult execute(Code code,
                                         Value[] arguments,
                                         TypeMetadata constantPool,
                                         @Nullable ClosureContext closureContext,
                                         CallContext callContext) {
        var stack = pool.borrowObject();
        try {
            return stack.execute0(code, arguments, constantPool, closureContext, callContext);
        }
        finally {
            pool.returnObject(stack);
        }
    }

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final ExceptionHandler[] exceptionHandlers = new ExceptionHandler[1024];
    private final Value[] stack = new Value[1024 * 1024];
    private final Frame[] frames = new Frame[1024];

    private VmStack() {
    }

    public static final int MAX_STEPS = 10000000;

    @SuppressWarnings({"DuplicatedCode", "UseCompareMethod", "DataFlowIssue", "ExtractMethodRecommender"})
    private @NotNull FlowExecResult execute0(Code code,
                                           Value[] arguments,
                                           TypeMetadata constantPool,
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
            var constants = constantPool.getValues();
            System.arraycopy(arguments, 0, stack, 0, arguments.length);
            var stack = this.stack;
            var frames = this.frames;
            int base = 0;
            int top = code.getMaxLocals();
            int handlerTop = 0;
            int pc = 0;
            var bytes = code.getCode();
            var fp = 0;
            ClassInstance exception = null;

            for (int s = 0; s < MAX_STEPS; s++) {
                var b = bytes[pc] & 0xff;
                try {
//                    if(DebugEnv.flag)
//                    log.debug("Executing bytecode {} at {}, top: {}", Bytecodes.getBytecodeName(b), pc, top);

                    except: {
                        switch (b) {
                            case Bytecodes.ADD_OBJECT -> {
                                int typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                boolean ephemeral = bytes[pc + 3] == 1;
                                var instance = ClassInstanceBuilder.newBuilder(type)
                                        .ephemeral(ephemeral)
                                        .build();
                                var fieldValues = new LinkedList<Value>();
                                var fields = type.getKlass().getAllFields();
                                int numFields = fields.size();
                                for (int i = 0; i < numFields; i++) {
                                    fieldValues.addFirst(stack[--top]);
                                }
                                NncUtils.biForEach(fields, fieldValues, (f, v) -> instance.initField(f, f.getType().fromStackValue(v)));
                                if (!instance.isEphemeral())
                                    callContext.instanceRepository().bind(instance);
                                stack[top++] = instance.getReference();
                                pc += 4;
                            }
                            case Bytecodes.SET_FIELD -> {
                                int fieldIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var field = (FieldRef) constants[fieldIndex];
                                var value = stack[--top];
                                var instance = stack[--top].resolveObject();
                                instance.setField(field.getRawField(), field.getType().fromStackValue(value));
                                pc += 3;
                            }
                            case Bytecodes.DELETE_OBJECT -> {
                                callContext.instanceRepository().remove(stack[--top].resolveDurable());
                                pc++;
                            }
                            case Bytecodes.RETURN -> {
                                var v = stack[top - 1];
                                Arrays.fill(stack, base, base + code.getFrameSize(), null);
                                if (fp == 0)
                                    return FlowExecResult.of(v);
                                stack[base] = v;
                                var frame = frames[--fp];
                                frames[fp] = null;
                                pc = frame.pc;
                                top = base + 1;
                                base = frame.base;
                                code = frame.code;
                                bytes = code.getCode();
                                constants = frame.constants;
                                closureContext = frame.closureContext;
                            }
                            case Bytecodes.RAISE -> {
                                exception = stack[--top].resolveObject();
                                break except;
                            }
                            case Bytecodes.INVOKE_VIRTUAL -> {
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var method = (MethodRef) constants[flowIndex];
                                int numCapturedVars = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                pc += 5;
                                if (numCapturedVars > 0) {
                                    var capturedVarIndexes = new int[numCapturedVars];
                                    var capturedVarTypes = new Type[numCapturedVars];
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarIndexes[i] = (bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff;
                                        pc += 2;
                                    }
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarTypes[i] = (Type) constants[(bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff];
                                        pc += 2;
                                    }
                                    method = (MethodRef) tryUncaptureFlow(method, capturedVarIndexes, capturedVarTypes, stack, base);
                                }
                                var self = stack[top - method.getParameterCount() - 1];
                                if (method.isVirtual())
                                    method = ((ClassType) requireNonNull(self).getType()).getOverride(method);
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid())
                                        stack[top++] = r.ret();
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    frames[fp++] = new Frame(pc, prevBase, base, code, constants, closureContext);
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
                                int numCapturedVars = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                pc += 5;
                                if (numCapturedVars > 0) {
                                    var capturedVarIndexes = new int[numCapturedVars];
                                    var capturedVarTypes = new Type[numCapturedVars];
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarIndexes[i] = (bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff;
                                        pc += 2;
                                    }
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarTypes[i] = (Type) constants[(bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff];
                                        pc += 2;
                                    }
                                    method = (MethodRef) tryUncaptureFlow(method, capturedVarIndexes, capturedVarTypes, stack, base);
                                }
                                var self = stack[top - method.getParameterCount() - 1];
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    top--;
                                    var r = NativeMethods.invoke(method.getRawFlow(), self, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid())
                                        stack[top++] = r.ret();
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount() - 1;
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    frames[fp++] = new Frame(pc, prevBase, base, code, constants, closureContext);
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
                                int numCapturedVars = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                pc += 5;
                                if (numCapturedVars > 0) {
                                    var capturedVarIndexes = new int[numCapturedVars];
                                    var capturedVarTypes = new Type[numCapturedVars];
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarIndexes[i] = (bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff;
                                        pc += 2;
                                    }
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarTypes[i] = (Type) constants[(bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff];
                                        pc += 2;
                                    }
                                    method = (MethodRef) tryUncaptureFlow(method, capturedVarIndexes, capturedVarTypes, stack, base);
                                }
                                if (method.isNative()) {
                                    var paramCount = method.getParameterCount();
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    var r = NativeMethods.invoke(method.getRawFlow(), null, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!method.getReturnType().isVoid())
                                        stack[top++] = r.ret();
                                } else {
                                    int prevBase = base;
                                    base = top - method.getParameterCount();
                                    top = base + method.getRawFlow().getCode().getMaxLocals();
                                    frames[fp++] = new Frame(pc, prevBase, base, code, constants, closureContext);
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
                                stack[top++] = result;
                                pc += 3;
                            }
                            case Bytecodes.NEW -> {
                                var typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                var ephemeral = bytes[pc + 3] == 1;
                                var unbound = bytes[pc + 4] == 1;
                                var self = ClassInstanceBuilder.newBuilder(type)
                                        .ephemeral(ephemeral)
                                        .closureContext(type.isLocal() ? new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)) : null)
                                        .build();
                                if (!self.isEphemeral() && !unbound)
                                    callContext.instanceRepository().bind(self);
                                stack[top++] = self.getReference();
                                pc += 5;
                            }
                            case Bytecodes.NEW_CHILD -> {
                                var typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var type = (ClassType) constants[typeIndex];
                                var self = ClassInstanceBuilder.newBuilder(type)
                                        .closureContext(type.isLocal() ? new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)) : null)
                                        .build();
                                var parent = stack[--top].resolveObject();
                                parent.addChild(self);
                                stack[top++] = self.getReference();
                                pc += 3;
                            }
                            case Bytecodes.SET_STATIC -> {
                                var field = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var sft = StaticFieldTable.getInstance(field.getDeclaringType(), ContextUtil.getEntityContext());
                                sft.set(field.getRawField(), stack[--top]);
                                pc += 3;
                            }
                            case Bytecodes.NEW_ARRAY -> {
                                // TODO support ephemeral
                                var array = new ArrayInstance((ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff]);
                                stack[top++] = array.getReference();
                                pc += 3;
                            }
                            case Bytecodes.TRY_ENTER -> {
                                var handler = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
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
                                frames[fp++] = new Frame(pc, prevBase, prevTop, code, constants, closureContext);
                                code = funcInst.getCode();
                                bytes = code.getCode();
                                constants = funcInst.getTypeMetadata().getValues();
                                closureContext = funcInst.getClosureContext(stack, base);
                                pc = 0;
                            }
                            case Bytecodes.LAMBDA -> {
                                var lambda = (LambdaRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var func = new LambdaValue(lambda, new ClosureContext(closureContext, Arrays.copyOfRange(stack, base, top)));
                                if (bytes[pc + 3] == 0) {
                                    stack[top++] = func;
                                    pc += 4;
                                } else {
                                    var functionalInterface = (ClassType) constants[bytes[pc + 4] | bytes[pc + 5]];
                                    // TODO Pre-generate functional interface implementation
                                    var functionInterfaceImpl = Types.createFunctionalClass(functionalInterface);
                                    var funcImplKlass = functionInterfaceImpl.getKlass();
                                    var funcField = funcImplKlass.getFieldByName("func");
                                    stack[top++] = ClassInstance.create(Map.of(funcField, func), functionInterfaceImpl).getReference();
                                    pc += 6;
                                }
                            }
                            case Bytecodes.ADD_ELEMENT -> {
                                var e = stack[--top];
                                var a = stack[--top].resolveArray();
                                a.addElement(a.getType().getElementType().fromStackValue(e));
                                pc++;
                            }
                            case Bytecodes.DELETE_ELEMENT -> {
                                var arrayInst = stack[--top].resolveArray();
                                var elementInst = stack[--top];
                                arrayInst.removeElement(elementInst);
                                pc++;
                            }
                            case Bytecodes.GET_ELEMENT -> {
                                var index = ((IntValue) stack[--top]).value;
                                var arrayInst = stack[--top].resolveArray();
                                if (index < arrayInst.size()) {
                                    stack[top++] = arrayInst.get(index).toStackValue();
                                    pc++;
                                } else {
                                    exception = ClassInstance.allocate(StdKlass.indexOutOfBoundsException.type());
                                    var nat = new IndexOutOfBoundsExceptionNative(exception);
                                    nat.IndexOutOfBoundsException(callContext);
                                    break except;
                                }
                            }
                            case Bytecodes.INVOKE_FUNCTION -> {
                                //noinspection DuplicatedCode
                                var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var func = (FunctionRef) constants[flowIndex];
                                int numCapturedVars = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                pc += 5;
                                if (numCapturedVars > 0) {
                                    //noinspection DuplicatedCode
                                    var capturedVarIndexes = new int[numCapturedVars];
                                    var capturedVarTypes = new Type[numCapturedVars];
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarIndexes[i] = (bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff;
                                        pc += 2;
                                    }
                                    for (int i = 0; i < numCapturedVars; i++) {
                                        capturedVarTypes[i] = (Type) constants[(bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff];
                                        pc += 2;
                                    }
                                    func = (FunctionRef) tryUncaptureFlow(func, capturedVarIndexes, capturedVarTypes, stack, base);
                                }
                                if (func.isNative()) {
                                    var paramCount = func.getParameterCount();
                                    var args = new Value[paramCount];
                                    for (int i = paramCount - 1; i >= 0; i--) {
                                        args[i] = stack[--top];
                                    }
                                    var nativeCode = Objects.requireNonNull(func.getRawFlow().getNativeCode());
                                    var r = nativeCode.run(func, List.of(args), callContext);
                                    if (r.exception() != null) {
                                        exception = r.exception();
                                        break except;
                                    } else if (!func.getReturnType().isVoid())
                                        stack[top++] = r.ret();
                                } else {
                                    int prevBase = base;
                                    base = top - func.getParameterCount();
                                    top = base + func.getRawFlow().getCode().getMaxLocals();
                                    frames[fp++] = new Frame(pc, prevBase, base, code, constants, closureContext);
                                    code = func.getRawFlow().getCode();
                                    bytes = code.getCode();
                                    constants = func.getTypeMetadata().getValues();
                                    closureContext = null;
                                    pc = 0;
                                }
                            }
                            case Bytecodes.CAST -> {
                                var inst = stack[--top];
                                var type = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                if (type.isInstance(inst)) {
                                    stack[top++] = inst;
                                    pc += 3;
                                } else if (type.isAssignableFrom(inst.getType())) {
                                    stack[top++] = inst;
                                    pc += 3;
                                } else {
                                    exception = ClassInstance.allocate(StdKlass.exception.get().getType());
                                    var exceptionNative = new ExceptionNative(exception);
                                    exceptionNative.Exception(new StringValue(
                                            String.format("Can not cast instance '%s' to type '%s'", inst.getTitle(), type.getName())
                                    ), callContext);
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
                                var copy = sourceInst.resolveDurable().copy();
                                stack[top++] = copy.getReference();
                                pc++;
                            }
                            case Bytecodes.INDEX_SCAN -> {
                                //noinspection DuplicatedCode
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var to = loadIndexKey(index, stack[--top]);
                                var from = loadIndexKey(index, stack[--top]);
                                var result = callContext.instanceRepository().indexScan(from, to);
                                var type = new ArrayType(index.getDeclaringType(), ArrayKind.READ_ONLY);
                                stack[top++] = new ArrayInstance(type, result).getReference();
                                pc += 3;
                            }
                            case Bytecodes.INDEX_COUNT -> {
                                //noinspection DuplicatedCode
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var to = loadIndexKey(index, stack[--top]);
                                var from = loadIndexKey(index, stack[--top]);
                                var count = callContext.instanceRepository().indexCount(from, to);
                                stack[top++] = new LongValue(count);
                                pc += 3;
                            }
                            case Bytecodes.INDEX_SELECT -> {
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var result = callContext.instanceRepository().indexSelect(loadIndexKey(index, stack[--top]));
                                var type = new KlassType(null, StdKlass.arrayList.get(), List.of(index.getDeclaringType()));
                                var list = ClassInstance.allocate(type);
                                var listNative = new ListNative(list);
                                listNative.List(callContext);
                                result.forEach(e -> listNative.add(e, callContext));
                                stack[top++] = list.getReference();
                                pc += 3;
                            }
                            case Bytecodes.INDEX_SELECT_FIRST -> {
                                var index = (IndexRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var result = callContext.instanceRepository().selectFirstByKey(loadIndexKey(index, stack[--top]));
                                stack[top++] = NncUtils.orElse(result, new NullValue());
                                pc += 3;
                            }
                            case Bytecodes.TARGET, Bytecodes.NOOP -> pc++;
                            case Bytecodes.NON_NULL -> {
                                var inst = stack[top - 1];
                                if (inst.isNull()) {
                                    exception = ClassInstance.allocate(StdKlass.nullPointerException.type());
                                    var nat = new NullPointerExceptionNative(exception);
                                    nat.NullPointerException(callContext);
                                    break except;
                                } else
                                    pc++;
                            }
                            case Bytecodes.SET_ELEMENT -> {
                                var e = stack[--top];
                                var i = ((IntValue) stack[--top]).value;
                                var a = stack[--top].resolveArray();
                                a.setElement(i, a.getType().getElementType().fromStackValue(e));
                                pc++;
                            }
                            case Bytecodes.IF_EQ -> {
                                if (((IntValue) stack[--top]).value == 0)
                                    pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                else
                                    pc += 3;
                            }
                            case Bytecodes.IF_NE -> {
                                if (((IntValue) stack[--top]).value != 0)
                                    pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                                else
                                    pc += 3;
                            }
                            case Bytecodes.GOTO -> pc += (short) ((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                            case Bytecodes.INT_ADD -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value + v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_SUB -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value - v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_MUL -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value * v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_DIV -> {
                                var v2 = ((IntValue) stack[--top]).value;
                                var v1 = ((IntValue) stack[--top]).value;
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(StdKlass.arithmeticException.type());
                                    var nat = new ArithmeticExceptionNative(exception);
                                    nat.ArithmeticException(Instances.stringInstance("/ by zero"), callContext);
                                    break except;
                                } else {
                                    stack[top++] = new IntValue(v1 / v2);
                                    pc++;
                                }
                            }
                            case Bytecodes.INT_REM -> {
                                var v2 = ((IntValue) stack[--top]).value;
                                var v1 = ((IntValue) stack[--top]).value;
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(StdKlass.arithmeticException.type());
                                    var nat = new ArithmeticExceptionNative(exception);
                                    nat.ArithmeticException(Instances.stringInstance("/ by zero"), callContext);
                                    break except;
                                } else {
                                    stack[top++] = new IntValue(v1 % v2);
                                    pc++;
                                }
                            }
                            case Bytecodes.LONG_ADD -> {
                                var v2 = (LongValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value + v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_SUB -> {
                                var v2 = (LongValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value - v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_MUL -> {
                                var v2 = (LongValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value * v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_DIV -> {
                                var v2 = ((LongValue) stack[--top]).value;
                                var v1 = ((LongValue) stack[--top]).value;
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(StdKlass.arithmeticException.type());
                                    var nat = new ArithmeticExceptionNative(exception);
                                    nat.ArithmeticException(Instances.stringInstance("/ by zero"), callContext);
                                    break except;
                                } else {
                                    stack[top++] = new LongValue(v1 / v2);
                                    pc++;
                                }
                            }
                            case Bytecodes.LONG_REM -> {
                                var v2 = ((LongValue) stack[--top]).value;
                                var v1 = ((LongValue) stack[--top]).value;
                                if (v2 == 0) {
                                    exception = ClassInstance.allocate(StdKlass.arithmeticException.type());
                                    var nat = new ArithmeticExceptionNative(exception);
                                    nat.ArithmeticException(Instances.stringInstance("/ by zero"), callContext);
                                    break except;
                                } else {
                                    stack[top++] = new LongValue(v1 % v2);
                                    pc++;
                                }
                            }
                            case Bytecodes.DOUBLE_ADD -> {
                                var v2 = (DoubleValue) stack[--top];
                                var v1 = (DoubleValue) stack[--top];
                                stack[top++] = new DoubleValue(v1.value + v2.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_SUB -> {
                                var v2 = (DoubleValue) stack[--top];
                                var v1 = (DoubleValue) stack[--top];
                                stack[top++] = new DoubleValue(v1.value - v2.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_MUL -> {
                                var v2 = (DoubleValue) stack[--top];
                                var v1 = (DoubleValue) stack[--top];
                                stack[top++] = new DoubleValue(v1.value * v2.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_DIV -> {
                                var v2 = (DoubleValue) stack[--top];
                                var v1 = (DoubleValue) stack[--top];
                                stack[top++] = new DoubleValue(v1.value / v2.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_REM -> {
                                var v2 = (DoubleValue) stack[--top];
                                var v1 = (DoubleValue) stack[--top];
                                stack[top++] = new DoubleValue(v1.value % v2.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_ADD -> {
                                var v2 = (FloatValue) stack[--top];
                                var v1 = (FloatValue) stack[--top];
                                stack[top++] = new FloatValue(v1.value + v2.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_SUB -> {
                                var v2 = (FloatValue) stack[--top];
                                var v1 = (FloatValue) stack[--top];
                                stack[top++] = new FloatValue(v1.value - v2.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_MUL -> {
                                var v2 = (FloatValue) stack[--top];
                                var v1 = (FloatValue) stack[--top];
                                stack[top++] = new FloatValue(v1.value * v2.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_DIV -> {
                                var v2 = (FloatValue) stack[--top];
                                var v1 = (FloatValue) stack[--top];
                                stack[top++] = new FloatValue(v1.value / v2.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_REM -> {
                                var v2 = (FloatValue) stack[--top];
                                var v1 = (FloatValue) stack[--top];
                                stack[top++] = new FloatValue(v1.value % v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_SHIFT_LEFT -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value << v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_SHIFT_RIGHT -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value >> v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_UNSIGNED_SHIFT_RIGHT -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value >>> v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_SHIFT_LEFT -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value << v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_SHIFT_RIGHT -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value >> v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_UNSIGNED_SHIFT_RIGHT -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value >>> v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_BIT_OR -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value | v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_BIT_AND -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value & v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_BIT_XOR -> {
                                var v2 = (IntValue) stack[--top];
                                var v1 = (IntValue) stack[--top];
                                stack[top++] = new IntValue(v1.value ^ v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_OR -> {
                                var v2 = (LongValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value | v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_AND -> {
                                var v2 = (LongValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value & v2.value);
                                pc++;
                            }
                            case Bytecodes.LONG_BIT_XOR -> {
                                var v2 = (LongValue) stack[--top];
                                var v1 = (LongValue) stack[--top];
                                stack[top++] = new LongValue(v1.value ^ v2.value);
                                pc++;
                            }
                            case Bytecodes.INT_NEG -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new IntValue(-v.value);
                                pc++;
                            }
                            case Bytecodes.LONG_NEG -> {
                                var v = (LongValue) stack[--top];
                                stack[top++] = new LongValue(-v.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_NEG -> {
                                var v = (DoubleValue) stack[--top];
                                stack[top++] = new DoubleValue(-v.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_NEG -> {
                                var v = (FloatValue) stack[--top];
                                stack[top++] = new FloatValue(-v.value);
                                pc++;
                            }
                            case Bytecodes.LONG_TO_DOUBLE -> {
                                var v = (LongValue) stack[--top];
                                stack[top++] = new DoubleValue(v.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_LONG -> {
                                var v = (DoubleValue) stack[--top];
                                stack[top++] = new LongValue((long) v.value);
                                pc++;
                            }
                            case Bytecodes.INT_TO_LONG -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new LongValue(v.value);
                                pc++;
                            }
                            case Bytecodes.INT_TO_CHAR -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new IntValue((char) v.value);
                                pc++;
                            }
                            case Bytecodes.INT_TO_SHORT -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new IntValue((short) v.value);
                                pc++;
                            }
                            case Bytecodes.INT_TO_BYTE -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new IntValue((byte) v.value);
                                pc++;
                            }
                            case Bytecodes.LONG_TO_INT -> {
                                var v = (LongValue) stack[--top];
                                stack[top++] = new IntValue((int) v.value);
                                pc++;
                            }
                            case Bytecodes.INT_TO_DOUBLE -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new DoubleValue(v.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_INT -> {
                                var v = (DoubleValue) stack[--top];
                                stack[top++] = new IntValue((int) v.value);
                                pc++;
                            }
                            case Bytecodes.INT_TO_FLOAT -> {
                                var v = (IntValue) stack[--top];
                                stack[top++] = new FloatValue((float) v.value);
                                pc++;
                            }
                            case Bytecodes.LONG_TO_FLOAT -> {
                                var v = (LongValue) stack[--top];
                                stack[top++] = new FloatValue((float) v.value);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_TO_FLOAT -> {
                                var v = (DoubleValue) stack[--top];
                                stack[top++] = new FloatValue((float) v.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_INT -> {
                                var v = (FloatValue) stack[--top];
                                stack[top++] = new IntValue((int) v.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_LONG -> {
                                var v = (FloatValue) stack[--top];
                                stack[top++] = new LongValue((long) v.value);
                                pc++;
                            }
                            case Bytecodes.FLOAT_TO_DOUBLE -> {
                                var v = (FloatValue) stack[--top];
                                stack[top++] = new DoubleValue(v.value);
                                pc++;
                            }
                            case Bytecodes.EQ -> {
                                var v = ((IntValue) stack[--top]).value;
                                stack[top++] = v == 0 ? one : zero;
                                pc++;
                            }
                            case Bytecodes.NE -> {
                                var v = ((IntValue) stack[--top]).value;
                                stack[top++] = v != 0 ? one : zero;
                                pc++;
                            }
                            case Bytecodes.GE -> {
                                var v = ((IntValue) stack[--top]).value;
                                stack[top++] = v >= 0 ? one : zero;
                                pc++;
                            }
                            case Bytecodes.GT -> {
                                var v = ((IntValue) stack[--top]).value;
                                stack[top++] = v > 0 ? one : zero;
                                pc++;
                            }
                            case Bytecodes.LT -> {
                                var v = ((IntValue) stack[--top]).value;
                                stack[top++] = v < 0 ? one : zero;
                                pc++;
                            }
                            case Bytecodes.LE -> {
                                var v = ((IntValue) stack[--top]).value;
                                stack[top++] = v <= 0 ? one : zero;
                                pc++;
                            }
                            case Bytecodes.INT_COMPARE -> {
                                var v2 = ((IntValue) stack[--top]).value;
                                var v1 = ((IntValue) stack[--top]).value;
                                var r = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                stack[top++] = new IntValue(r);
                                pc++;
                            }
                            case Bytecodes.LONG_COMPARE -> {
                                var v2 = ((LongValue) stack[--top]).value;
                                var v1 = ((LongValue) stack[--top]).value;
                                var r = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                stack[top++] = new IntValue(r);
                                pc++;
                            }
                            case Bytecodes.DOUBLE_COMPARE -> {
                                var v2 = ((DoubleValue) stack[--top]).value;
                                var v1 = ((DoubleValue) stack[--top]).value;
                                var r = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                stack[top++] = new IntValue(r);
                                pc++;
                            }
                            case Bytecodes.FLOAT_COMPARE -> {
                                var v2 = ((FloatValue) stack[--top]).value;
                                var v1 = ((FloatValue) stack[--top]).value;
                                var r = (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
                                stack[top++] = new IntValue(r);
                                pc++;
                            }
                            case Bytecodes.REF_COMPARE_EQ -> {
                                var v2 = (Value) stack[--top];
                                var v1 = (Value) stack[--top];
                                stack[top++] = v1.equals(v2) ? one : zero;
                                pc++;
                            }
                            case Bytecodes.REF_COMPARE_NE -> {
                                var v2 = (Value) stack[--top];
                                var v1 = (Value) stack[--top];
                                stack[top++] = !v1.equals(v2) ? one : zero;
                                pc++;
                            }
                            case Bytecodes.GET_FIELD -> {
                                var i = stack[--top].resolveObject();
                                var p = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top++] = i.getField(p.getRawField()).toStackValue();
                                pc += 3;
                            }
                            case Bytecodes.GET_METHOD -> {
                                var i = stack[--top].resolveObject();
                                var methodRef = (MethodRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top++] = i.getFunction(methodRef);
                                pc += 3;
                            }
                            case Bytecodes.GET_STATIC_FIELD -> {
                                var fieldRef = (FieldRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var staticFieldTable = StaticFieldTable.getInstance(fieldRef.getDeclaringType(), ContextUtil.getEntityContext());
                                stack[top++] = staticFieldTable.get(fieldRef.getRawField()).toStackValue();
                                pc += 3;
                            }
                            case Bytecodes.GET_STATIC_METHOD -> {
                                var methodRef = (MethodRef) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top++] = new FlowValue(methodRef, null);
                                pc += 3;
                            }
                            case Bytecodes.INSTANCE_OF -> {
                                var v = stack[--top];
                                var targetType = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top++] = targetType.isInstance(v) ? one : zero;
                                pc += 3;
                            }
                            case Bytecodes.ARRAY_LENGTH -> {
                                var a = stack[--top].resolveArray();
                                stack[top++] = new IntValue(a.length());
                                pc++;
                            }
                            case Bytecodes.STORE -> {
                                var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                stack[base + index] = stack[--top];
                                pc += 3;
                            }
                            case Bytecodes.LOAD -> {
                                var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                stack[top++] = stack[base + index];
                                pc += 3;
                            }
                            case Bytecodes.LOAD_CONTEXT_SLOT -> {
                                var contextIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var slotIndex = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                stack[top++] = Objects.requireNonNull(closureContext).get(contextIndex, slotIndex);
                                pc += 5;
                            }
                            case Bytecodes.STORE_CONTEXT_SLOT -> {
                                var contextIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                                var slotIndex = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                Objects.requireNonNull(closureContext).set(contextIndex, slotIndex, stack[--top]);
                                pc += 5;
                            }
                            case Bytecodes.LOAD_CONSTANT -> {
                                var value = (Value) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                stack[top++] = value;
                                pc += 3;
                            }
                            case Bytecodes.NEW_ARRAY_WITH_DIMS -> {
                                var type = (ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var array = new ArrayInstance(type);
                                var dimensions = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                                var dims = new int[dimensions];
                                for (int i = dimensions - 1; i >= 0; i--) {
                                    dims[i] = ((IntValue) stack[--top]).value;
                                }
                                Instances.initArray(array, dims, 0);
                                stack[top++] = array.getReference();
                                pc += 5;
                            }
                            case Bytecodes.VOID_RETURN -> {
                                Arrays.fill(stack, base, base + code.getFrameSize(), null);
                                if (fp == 0)
                                    return FlowExecResult.of(null);
                                var frame = frames[--fp];
                                frames[fp] = null;
                                pc = frame.pc;
                                top = base;
                                base = frame.base;
                                code = frame.code;
                                bytes = code.getCode();
                                constants = frame.constants;
                                closureContext = frame.closureContext;
                            }
                            case Bytecodes.LOAD_TYPE -> {
                                var type = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                                var klass = Types.getKlass(type);
                                stack[top++] = ContextUtil.getEntityContext().getInstance(klass).getReference();
                                pc += 3;
                            }
                            case Bytecodes.DUP -> {
                                stack[top] = stack[top++ - 1];
                                pc++;
                            }
                            case Bytecodes.POP -> {
                                stack[--top] = null;
                                pc++;
                            }
                            case Bytecodes.DUP_X1 -> {
                                var v = stack[top] = stack[top - 1];
                                stack[top - 1] = stack[top - 2];
                                stack[top - 2] = v;
                                top++;
                                pc++;
                            }
                            case Bytecodes.DUP_X2 -> {
                                var v = stack[top] = stack[top - 1];
                                stack[top - 1] = stack[top - 2];
                                stack[top - 2] = stack[top - 3];
                                stack[top - 3] = v;
                                top++;
                                pc++;
                            }
                            case Bytecodes.LOAD_PARENT -> {
                                var v = stack[--top];
                                var idx = (bytes[pc + 1] & 0xff) << 8 | (bytes[pc + 2] & 0xff);
                                stack[top++] = requireNonNull(v.resolveObject().getParent(idx)).getReference();
                                pc += 3;
                            }
                            case Bytecodes.TABLE_SWITCH -> {
                                var k = ((IntValue) stack[--top]).value;
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
                                var k = ((IntValue) stack[--top]).value;
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
                            Arrays.fill(stack, f.base + f.code.getFrameSize(), base + code.getFrameSize(), null);
                            fp = h.fp;
                            base = f.base;
                            top = f.top;
                            code = f.code;
                            bytes = code.getCode();
                            pc = h.pc;
                            constants = f.constants;
                            closureContext = f.closureContext;
                        }
                        stack[top++] = exception.getReference();
                    }
                    else {
                        Arrays.fill(stack, 0, base + code.getFrameSize(), null);
                        return FlowExecResult.ofException(exception);
                    }

                } catch (Exception e) {
                    throw new InternalException("Failed to execute node " + Bytecodes.getBytecodeName(b) +  " at " + pc
                            + " in flow " + code.getFlow().getQualifiedName(), e);
                }
            }
            throw new FlowExecutionException(String.format("Flow execution steps exceed the limit: %d", MAX_STEPS));
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
            actualExprTypes[i] = stack[base + capturedVariableIndexes[i]].getType();
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
                && NncUtils.anyMatch(method.getDeclaringType().getTypeArguments(), Type::isCaptured)) {
            var declaringType = method.getDeclaringType();
            var actualTypeArgs = NncUtils.map(declaringType.getTypeArguments(), t -> t.accept(typeSubst));
            var actualDeclaringType = new KlassType(declaringType.getOwner(), declaringType.getKlass(), actualTypeArgs);
            if(DebugEnv.debugging)
                log.info("uncapture flow declaring type from {} to {}",
                        declaringType.getTypeDesc(),
                        actualDeclaringType.getTypeDesc());
            flow = NncUtils.requireNonNull(actualDeclaringType.findSelfMethod(m -> m.getRawFlow() == method.getRawFlow()));
        }
        if(NncUtils.anyMatch(flow.getTypeArguments(), Type::isCaptured)) {
            var actualTypeArgs = NncUtils.map(flow.getTypeArguments(), t -> t.accept(typeSubst));
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
        Code code,
        Object[] constants,
        ClosureContext closureContext) {
    }

    private record ExceptionHandler(int fp, int pc) {}

}
