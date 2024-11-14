package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ExceptionNative;
import org.metavm.entity.natives.ListNative;
import org.metavm.entity.natives.NullPointerExceptionNative;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class MetaFrame implements Frame, CallContext {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final InstanceRepository instanceRepository;
    private final LinkedList<Integer> tryExits = new LinkedList<>();
    private ClassInstance exception;

    private final Map<Integer, ClassInstance> exceptions = new IdentityHashMap<>();

    public MetaFrame(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    public void addInstance(Instance instance) {
        instanceRepository.bind(instance);
    }

    public int catchException(@NotNull ClassInstance exception) {
        var tryExit = tryExits.peek();
        if(tryExit != null) {
            exceptions.put(tryExit, exception);
            return tryExit;
        }
        else {
            this.exception = exception;
            return -1;
        }
    }

    public void deleteInstance(Reference instance) {
        instanceRepository.remove(instance.resolve());
    }

    public static final int MAX_STEPS = 100000;

    @SuppressWarnings("DuplicatedCode")
    public @NotNull FlowExecResult execute(ScopeRT scope,
                                           Value[] arguments,
                                           @Nullable ClosureContext closureContext) {
//        if(DebugEnv.flag) {
//            log.debug("Executing flow {}, maxLocals: {}, maxStack: {}, constants: {}, code length: {}",
//                    scope.getCallable(), scope.getMaxLocals(), scope.getMaxStack(), scope.getConstantPool().getEntries().size(),
//                    scope.getCode().length);
//            log.debug("{}", EncodingUtils.bytesToHex(scope.getCode()));
//            log.debug("Constants: {}", Arrays.toString(scope.getConstantPool().getResolvedValues()));
//        }
        var pc = 0;
        var bytes = scope.getCode();
        var constants = scope.getFlow().getConstantPool().getResolvedValues();
        var locals = new Value[scope.getMaxLocals()];
        System.arraycopy(arguments, 0, locals, 0, arguments.length);
        var top = 0;
        var stack = new Value[scope.getMaxStack()];
        try {
            for (int s = 0; s < MAX_STEPS; s++) {
                var b = bytes[pc] & 0xff;
                try {
//                    if(DebugEnv.flag)
//                        log.debug("Executing bytecode {} at {}, top: {}", Bytecodes.getBytecodeName(b), pc, top);
                    switch (b) {
                        case Bytecodes.ADD_OBJECT -> {
                            int typeIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            var type = (ClassType) constants[typeIndex];
                            boolean ephemeral = bytes[pc + 3] == 1;
                            var instance = ClassInstanceBuilder.newBuilder(type)
                                    .ephemeral(ephemeral)
                                    .build();
                            var fieldValues = new LinkedList<Value>();
                            var fields = type.resolve().getAllFields();
                            int numFields = fields.size();
                            for (int i = 0; i < numFields; i++) {
                                fieldValues.addFirst(stack[--top]);
                            }
                            NncUtils.biForEach(fields, fieldValues, instance::initField);
                            if (!instance.isEphemeral())
                                addInstance(instance);
                            stack[top++] = instance.getReference();
                            pc += 4;
                        }
                        case Bytecodes.SET_FIELD -> {
                            int fieldIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            var field = (Field) constants[fieldIndex];
                            var value = stack[--top];
                            var instance = stack[--top].resolveObject();
                            instance.setField(field, value);
                            pc += 3;
                        }
                        case Bytecodes.DELETE_OBJECT -> {
                            deleteInstance((Reference) stack[--top]);
                            pc++;
                        }
                        case Bytecodes.RETURN -> {
                            return new FlowExecResult(stack[--top], null);
                        }
                        case Bytecodes.RAISE -> {
                            if ((pc = catchException(stack[--top].resolveObject())) == -1)
                                return raise();
                        }
                        case Bytecodes.METHOD_CALL -> {
                            var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            var method = (Method) constants[flowIndex];
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
                                method = (Method) tryUncaptureFlow(method, capturedVarIndexes, capturedVarTypes, locals);
                            }
                            var args = new LinkedList<Value>();
                            int numArgs = method.getParameters().size();
                            for (int j = 0; j < numArgs; j++) {
                                args.addFirst(stack[--top]);
                            }
                            var self = method.isStatic() ? null : stack[--top].resolveObject();
                            if (method.isVirtual())
                                method = requireNonNull(self).getKlass().resolveMethod(method);
                            //noinspection DuplicatedCode
                            FlowExecResult result = method.execute(self, args, this);
                            if (result.exception() != null) {
                                if ((pc = catchException(result.exception())) == -1)
                                    return new FlowExecResult(null, Objects.requireNonNull(exception));
                            } else {
                                if (result.ret() != null)
                                    stack[top++] = result.ret();
                            }
                        }
                        case Bytecodes.GET_UNIQUE -> {
                            var index = (Index) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            Value result = instanceRepository().selectFirstByKey(loadIndexKey(index, stack, top));
                            top -= index.getFields().size();
                            if (result == null)
                                result = Instances.nullInstance();
                            stack[top++] = result;
                            pc += 3;
                        }
                        case Bytecodes.NEW -> {
                            //noinspection DuplicatedCode
                            var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            var method = (Method) constants[flowIndex];
                            int numCapturedVars = (bytes[pc + 3] & 0xff) << 8 | bytes[pc + 4] & 0xff;
                            pc += 5;
                            if (numCapturedVars > 0) {
                                var capturedVarIndexes = new int[numCapturedVars];
                                var capturedVarTypes = new Type[numCapturedVars];
                                //noinspection DuplicatedCode
                                for (int i = 0; i < numCapturedVars; i++) {
                                    capturedVarIndexes[i] = (bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff;
                                    pc += 2;
                                }
                                for (int i = 0; i < numCapturedVars; i++) {
                                    capturedVarTypes[i] = (Type) constants[(bytes[pc] & 0xff) << 8 | bytes[pc + 1] & 0xff];
                                    pc += 2;
                                }
                                method = (Method) tryUncaptureFlow(method, capturedVarIndexes, capturedVarTypes, locals);
                            }
                            var ephemeral = bytes[pc] == 1;
                            var unbound = bytes[pc + 1] == 1;
                            pc += 2;
                            var args = new LinkedList<Value>();
                            int numArgs = method.getParameters().size();
                            for (int j = 0; j < numArgs; j++) {
                                args.addFirst(stack[--top]);
                            }
                            var type = method.getDeclaringType().getType();
                            var self = ClassInstanceBuilder.newBuilder(type)
                                    .ephemeral(ephemeral)
                                    .build();
                            if (!self.isEphemeral() && !unbound)
                                addInstance(self);
                            //noinspection DuplicatedCode
                            FlowExecResult result = method.execute(self, args, this);
                            if (result.exception() != null) {
                                pc = catchException(result.exception());
                                if (pc == -1)
                                    return new FlowExecResult(null, Objects.requireNonNull(exception));
                            } else {
                                if (result.ret() != null)
                                    stack[top++] = result.ret();
                            }
                        }
                        case Bytecodes.SET_STATIC -> {
                            var field = (Field) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var sft = StaticFieldTable.getInstance(field.getDeclaringType(), ContextUtil.getEntityContext());
                            sft.set(field, stack[--top]);
                            pc += 3;
                        }
                        case Bytecodes.NEW_ARRAY -> {
                            // TODO support ephemeral
                            var array = new ArrayInstance((ArrayType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff]);
                            stack[top++] = array.getReference();
                            pc += 3;
                        }
                        case Bytecodes.TRY_ENTER -> {
                            tryExits.push((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff);
                            pc += 3;
                        }
                        case Bytecodes.TRY_EXIT -> {
                            var tryExit = tryExits.pop();
                            assert tryExit == pc;
                            var exception = exceptions.get(tryExit);
                            if (exception != null)
                                stack[top++] = exception.getReference();
                            else
                                stack[top++] = Instances.nullInstance();
                            locals[((bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff)] = stack[--top];
                            pc += 3;
                        }
                        case Bytecodes.FUNC -> {
                            var functionType = (FunctionType) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var args = new LinkedList<org.metavm.object.instance.core.Value>();
                            var numArgs = functionType.getParameterTypes().size();
                            for (int j = 0; j < numArgs; j++)
                                args.addFirst(stack[--top]);
                            var funcInst = (FunctionValue) stack[--top];
                            var result = funcInst.execute(args, this);
                            if (result.exception() != null) {
                                if ((pc = catchException(result.exception())) == -1)
                                    return new FlowExecResult(null, Objects.requireNonNull(exception));
                            } else {
                                if (result.ret() != null)
                                    stack[top++] = result.ret();
                                pc += 3;
                            }
                        }
                        case Bytecodes.LAMBDA -> {
                            var lambda = (Lambda) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var func = new LambdaValue(lambda, new ClosureContext(closureContext, locals));
                            if (bytes[pc + 3] == 0) {
                                stack[top++] = func;
                                pc += 4;
                            } else {
                                var functionalInterface = (ClassType) constants[bytes[pc + 4] | bytes[pc + 5]];
                                // TODO Pre-generate functional interface implementation
                                var functionInterfaceImpl = Types.createFunctionalClass(functionalInterface);
                                var funcImplKlass = functionInterfaceImpl.resolve();
                                var funcField = funcImplKlass.getFieldByCode("func");
                                stack[top++] = ClassInstance.create(Map.of(funcField, func), functionInterfaceImpl).getReference();
                                pc += 6;
                            }
                        }
                        case Bytecodes.ADD_ELEMENT -> {
                            var e = stack[--top];
                            var a = stack[--top].resolveArray();
                            a.addElement(e);
                            pc++;
                        }
                        case Bytecodes.DELETE_ELEMENT -> {
                            var arrayInst = stack[--top].resolveArray();
                            var elementInst = stack[--top];
                            stack[top++] = Instances.booleanInstance(arrayInst.removeElement(elementInst));
                            pc++;
                        }
                        case Bytecodes.GET_ELEMENT -> {
                            var indexInst = (LongValue) stack[--top];
                            var arrayInst = stack[--top].resolveArray();
                            stack[top++] = arrayInst.get(indexInst.getValue().intValue());
                            pc++;
                        }
                        case Bytecodes.FUNCTION_CALL -> {
                            //noinspection DuplicatedCode
                            var flowIndex = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            var func = (Function) constants[flowIndex];
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
                                func = (Function) tryUncaptureFlow(func, capturedVarIndexes, capturedVarTypes, locals);
                            }
                            var args = new LinkedList<Value>();
                            int numArgs = func.getParameters().size();
                            for (int j = 0; j < numArgs; j++) {
                                args.addFirst(stack[--top]);
                            }
                            FlowExecResult result = func.execute(null, args, this);
                            if (result.exception() != null) {
                                if ((pc = catchException(result.exception())) == -1)
                                    return raise();
                            } else {
                                if (result.ret() != null)
                                    stack[top++] = result.ret();
                            }
                        }
                        case Bytecodes.CAST -> {
                            var inst = stack[--top];
                            var type = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            if (type.isInstance(inst)) {
                                stack[top++] = inst;
                                pc += 3;
                            } else if (type.isConvertibleFrom(inst.getType())) {
                                stack[top++] = type.convert(inst);
                                pc += 3;
                            } else {
                                var exception = ClassInstance.allocate(StdKlass.exception.get().getType());
                                var exceptionNative = new ExceptionNative(exception);
                                exceptionNative.Exception(Instances.stringInstance(
                                        String.format("Can not cast instance '%s' to type '%s'", inst.getTitle(), type.getName())
                                ), this);
                                if ((pc = catchException(exception)) == -1)
                                    return raise();
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
                        case Bytecodes.MAP -> {
                            var sourceInst = (Reference) stack[--top];
                            var mapping = (ObjectMapping) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            stack[top++] = mapping.map(sourceInst.resolve(), this).getReference();
                            pc += 3;
                        }
                        case Bytecodes.UNMAP -> {
                            var viewInst = (Reference) stack[--top];
                            var mapping = (ObjectMapping) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            stack[top++] = mapping.unmap(viewInst, this);
                            pc += 3;
                        }
                        case Bytecodes.INDEX_SCAN -> {
                            //noinspection DuplicatedCode
                            var index = (Index) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var to = loadIndexKey(index, stack, top);
                            top -= index.getFields().size();
                            var from = loadIndexKey(index, stack, top);
                            top -= index.getFields().size();
                            var result = instanceRepository().indexScan(from, to);
                            var type = new ArrayType(index.getDeclaringType().getType(), ArrayKind.READ_ONLY);
                            stack[top++] = new ArrayInstance(type, result).getReference();
                            pc += 3;
                        }
                        case Bytecodes.INDEX_COUNT -> {
                            //noinspection DuplicatedCode
                            var index = (Index) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var to = loadIndexKey(index, stack, top);
                            top -= index.getFields().size();
                            var from = loadIndexKey(index, stack, top);
                            top -= index.getFields().size();
                            var count = instanceRepository().indexCount(from, to);
                            stack[top++] = Instances.longInstance(count);
                            pc += 3;
                        }
                        case Bytecodes.INDEX_SELECT -> {
                            var index = (Index) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var result = instanceRepository().indexSelect(loadIndexKey(index, stack, top));
                            top -= index.getFields().size();
                            var type = new ClassType(StdKlass.arrayList.get(), List.of(index.getDeclaringType().getType()));
                            var list = ClassInstance.allocate(type);
                            var listNative = new ListNative(list);
                            listNative.List(this);
                            result.forEach(e -> listNative.add(e, this));
                            stack[top++] = list.getReference();
                            pc += 3;
                        }
                        case Bytecodes.INDEX_SELECT_FIRST -> {
                            var index = (Index) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var result = instanceRepository().selectFirstByKey(loadIndexKey(index, stack, top));
                            top -= index.getFields().size();
                            stack[top++] = NncUtils.orElse(result, Instances.nullInstance());
                            pc += 3;
                        }
                        case Bytecodes.GOTO -> pc = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                        case Bytecodes.TARGET, Bytecodes.NOOP -> pc++;
                        case Bytecodes.NON_NULL -> {
                            var inst = stack[top - 1];
                            if (inst.isNull()) {
                                var npe = ClassInstance.allocate(StdKlass.nullPointerException.type());
                                var nat = new NullPointerExceptionNative(npe);
                                nat.NullPointerException(this);
                                if ((pc = catchException(npe)) == -1)
                                    return raise();
                            } else
                                pc++;
                        }
                        case Bytecodes.SET_ELEMENT -> {
                            var e = stack[--top];
                            var i = ((LongValue) stack[--top]).getValue().intValue();
                            var a = stack[--top].resolveArray();
                            a.setElement(i, e);
                            pc++;
                        }
                        case Bytecodes.IF -> {
                            if (((BooleanValue) stack[--top]).getValue())
                                pc = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            else
                                pc += 3;
                        }
                        case Bytecodes.ADD -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.add(v2);
                            pc++;
                        }
                        case Bytecodes.SUB -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.sub(v2);
                            pc++;
                        }
                        case Bytecodes.MUL -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.mul(v2);
                            pc++;
                        }
                        case Bytecodes.DIV -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.div(v2);
                            pc++;
                        }
                        case Bytecodes.LEFT_SHIFT -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.leftShift(v2);
                            pc++;
                        }
                        case Bytecodes.RIGHT_SHIFT -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.rightShift(v2);
                            pc++;
                        }
                        case Bytecodes.UNSIGNED_RIGHT_SHIFT -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.unsignedRightShift(v2);
                            pc++;
                        }
                        case Bytecodes.BIT_OR -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.bitOr(v2);
                            pc++;
                        }
                        case Bytecodes.BIT_AND -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.bitAnd(v2);
                            pc++;
                        }
                        case Bytecodes.BIT_XOR -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.bitXor(v2);
                            pc++;
                        }
                        case Bytecodes.AND -> {
                            var v2 = (BooleanValue) stack[--top];
                            var v1 = (BooleanValue) stack[--top];
                            stack[top++] = v1.and(v2);
                            pc++;
                        }
                        case Bytecodes.OR -> {
                            var v2 = (BooleanValue) stack[--top];
                            var v1 = (BooleanValue) stack[--top];
                            stack[top++] = v1.or(v2);
                            pc++;
                        }
                        case Bytecodes.BIT_NOT -> {
                            var v = (LongValue) stack[--top];
                            stack[top++] = v.bitNot();
                            pc++;
                        }
                        case Bytecodes.NOT -> {
                            var v1 = (BooleanValue) stack[--top];
                            stack[top++] = v1.not();
                            pc++;
                        }
                        case Bytecodes.NEGATE -> {
                            var v = (NumberValue) stack[--top];
                            stack[top++] = v.negate();
                            pc++;
                        }
                        case Bytecodes.REM -> {
                            var v2 = (LongValue) stack[--top];
                            var v1 = (LongValue) stack[--top];
                            stack[top++] = v1.rem(v2);
                            pc++;
                        }
                        case Bytecodes.EQ -> {
                            var v2 = stack[--top];
                            var v1 = stack[--top];
                            stack[top++] = Instances.equals(v1, v2);
                            pc++;
                        }
                        case Bytecodes.NE -> {
                            var v2 = stack[--top];
                            var v1 = stack[--top];
                            stack[top++] = Instances.notEquals(v1, v2);
                            pc++;
                        }
                        case Bytecodes.GE -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.ge(v2);
                            pc++;
                        }
                        case Bytecodes.GT -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.gt(v2);
                            pc++;
                        }
                        case Bytecodes.LT -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.lt(v2);
                            pc++;
                        }
                        case Bytecodes.LE -> {
                            var v2 = (NumberValue) stack[--top];
                            var v1 = (NumberValue) stack[--top];
                            stack[top++] = v1.le(v2);
                            pc++;
                        }
                        case Bytecodes.GET_PROPERTY -> {
                            var i = stack[--top].resolveObject();
                            var p = (Property) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            stack[top++] = i.getProperty(p);
                            pc += 3;
                        }
                        case Bytecodes.GET_STATIC -> {
                            var property = (Property) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            if (property instanceof Field field) {
                                var staticFieldTable = StaticFieldTable.getInstance(field.getDeclaringType(), ContextUtil.getEntityContext());
                                stack[top++] = staticFieldTable.get(field);
                            } else if (property instanceof Method method) {
                                stack[top++] = new FlowValue(method, null);
                            } else
                                throw new IllegalStateException("Unknown property type: " + property);
                            pc += 3;
                        }
                        case Bytecodes.INSTANCE_OF -> {
                            var v = stack[--top];
                            var targetType = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            stack[top++] = Instances.booleanInstance(targetType.isInstance(v));
                            pc += 3;
                        }
                        case Bytecodes.ARRAY_LENGTH -> {
                            var a = stack[--top].resolveArray();
                            stack[top++] = Instances.longInstance(a.length());
                            pc++;
                        }
                        case Bytecodes.IF_NOT -> {
                            if (((BooleanValue) stack[--top]).getValue())
                                pc += 3;
                            else
                                pc = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                        }
                        case Bytecodes.STORE -> {
                            var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            locals[index] = stack[--top];
                            pc += 3;
                        }
                        case Bytecodes.LOAD -> {
                            var index = (bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff;
                            stack[top++] = locals[index];
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
                                dims[i] = ((LongValue) stack[--top]).getValue().intValue();
                            }
                            Instances.initArray(array, dims, 0);
                            stack[top++] = array.getReference();
                            pc += 5;
                        }
                        case Bytecodes.VOID_RETURN -> {
                            return new FlowExecResult(null, null);
                        }
                        case Bytecodes.LOAD_TYPE -> {
                            var type = (Type) constants[(bytes[pc + 1] & 0xff) << 8 | bytes[pc + 2] & 0xff];
                            var klass = Types.getKlass(type);
                            stack[top++] = ContextUtil.getEntityContext().getInstance(klass.getEffectiveTemplate()).getReference();
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
                        default -> throw new IllegalStateException("Invalid bytecode: " + b);
                    }
                } catch (Exception e) {
                    throw new InternalException("Failed to execute node " + Bytecodes.getBytecodeName(b) +  " at " + pc
                            + " in flow " + scope.getFlow().getQualifiedName(), e);
                }
            }
            throw new FlowExecutionException(String.format("Flow execution steps exceed the limit: %d", MAX_STEPS));
        } finally {
//            if(DebugEnv.flag)
//                log.debug("Exiting flow {}", scope.getFlow().getQualifiedName());
        }
    }

    private FlowExecResult raise() {
        return new FlowExecResult(null, Objects.requireNonNull(exception));
    }


    private Flow tryUncaptureFlow(Flow flow, int[] capturedVariableIndexes, Type[] capturedVariableTypes, Value[] locals) {
        if(capturedVariableIndexes.length == 0)
            return flow;
        var actualExprTypes = new Type[capturedVariableIndexes.length];
        for (int i = 0; i < capturedVariableIndexes.length; i++) {
            actualExprTypes[i] = locals[capturedVariableIndexes[i]].getType();
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
        if(flow instanceof Method method && method.getDeclaringType().isParameterized()
                && NncUtils.anyMatch(method.getDeclaringType().getTypeArguments(), Type::isCaptured)) {
            var declaringType = method.getDeclaringType();
            var actualTypeArgs = NncUtils.map(declaringType.getTypeArguments(), t -> t.accept(typeSubst));
            var actualDeclaringType = declaringType.getEffectiveTemplate().getParameterized(actualTypeArgs);
            if(DebugEnv.debugging)
                log.info("uncapture flow declaring type from {} to {}",
                        declaringType.getTypeDesc(),
                        actualDeclaringType.getTypeDesc());
            flow = NncUtils.requireNonNull(actualDeclaringType.findSelfMethod(
                    m -> m.getEffectiveVerticalTemplate() == method.getEffectiveVerticalTemplate()));
        }
        if(NncUtils.anyMatch(flow.getTypeArguments(), Type::isCaptured)) {
            var actualTypeArgs = NncUtils.map(flow.getTypeArguments(), t -> t.accept(typeSubst));
            return Objects.requireNonNull(flow.getHorizontalTemplate()).getParameterized(actualTypeArgs);
        }
        else
            return flow;
    }

    @SuppressWarnings("unused")
    private void checkResult(Value result, NodeRT node) {
        Type outputType = node.getType();
        if (outputType == null || outputType.isVoid()) {
            if (result != null) {
                throw new InternalException("Node " + node + " can not return a result value");
            }
        } else {
            if (!outputType.isInstance(result)) {
                throw new InternalException("Node " + node + " returned a result '" + result
                        + "' that is not an instance of the output type: " + outputType);
            }
        }
    }

    public ClassInstance getThrow() {
        return NncUtils.requireNonNull(exception);
    }

    @Override
    public InstanceRepository instanceRepository() {
        return instanceRepository;
    }

    public IndexKeyRT loadIndexKey(Index index, Value[] stack, int top) {
        var values = new LinkedList<Value>();
        var numFields = index.getFields().size();
        for (int i = 0; i < numFields; i++) {
            values.addFirst(stack[--top]);
        }
        return index.createIndexKey(values);
    }

}
