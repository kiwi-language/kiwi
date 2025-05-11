package org.metavm.compiler.generate;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.element.*;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.Buffer;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.metavm.flow.Bytecodes.*;

@Slf4j
public class Code {
    private final Executable executable;
    private final ConstPool constPool;
    private final Buffer buffer = new Buffer();
    private int pc;
    private State state = new State();
    private boolean alive = true;
    private int maxStack;
    private int maxLocals;
    private @Nullable Chain pendingJumps;

    public Code(Executable executable) {
        this.executable = executable;
        constPool = executable.getConstPool();
    }

    public void loadThis(Env env) {
        if (executable instanceof Method method) {
            code(LOAD);
            index(0);
            state.push(method.getDeclClass());
        } else {
           var method = env.currentMethod();
            code(LOAD_CONTEXT_SLOT);
            index(env.getContextIndex(method));
            index(0);
            state.push(method.getDeclClass());
        }
    }

    public void loadParent(int index, ClassType type) {
        code(LOAD_PARENT);
        index(index);
        state.pop();
        state.push(type);
    }

    public void reset(@Nullable Type top) {
        state = new State();
        if (top != null)
            state.push(top);
        alive = true;
    }

    public void tryEnter() {
        code(TRY_ENTER);
        index(0);
    }

    public void tryExit() {
        code(TRY_EXIT);
    }

    public void is(Type type) {
        code(INSTANCE_OF);
        constant(type);
    }

    public void load(LocalVar v) {
        load(v.getIndex(), v.getType());
    }

    public void load(int index, Type type) {
        code(LOAD);
        index(index);
        state.push(type);

    }

    public void store(int v) {
        code(STORE);
        index(v);
        state.pop();
    }

    public void getField(FieldRef field) {
        code(GET_FIELD);
        constant(field);
        state.pop();
        state.push(field.getType().toStackType());
    }

    public void invokeVirtual(MethodRef method) {
        code(INVOKE_VIRTUAL);
        constant(method);
        state.pop(method.getParamTypes().size() + 1);
        pushReturnType(method.getRetType());
    }

    public void invokeStatic(MethodRef method) {
        code(INVOKE_STATIC);
        constant(method);
        state.pop(method.getParamTypes().size());
        pushReturnType(method.getRetType());
    }

    public void invokeSpecial(MethodRef method) {
        code(INVOKE_SPECIAL);
        constant(method);
        state.pop(method.getParamTypes().size() + 1);
        pushReturnType(method.getRetType());
    }

    public void call(FuncType type) {
        code(FUNC);
        constant(type);
        state.pop(1 + type.getParamTypes().size());
        pushReturnType(type.getRetType());
    }

    private void pushReturnType(Type retType) {
        if (retType != PrimitiveType.VOID)
            state.push(retType.toStackType());
    }

    public void new_(ClassType type) {
        code(NEW);
        constant(type);
        false_();
        false_();
        state.push(type);
    }

    public void newChild(ClassType type) {
        code(NEW_CHILD);
        constant(type);
        state.pop();
        state.push(type);
    }

//    int lastCode = -1;

    public void code(int code) {
//        if (lastCode != -1)
//            log.debug("{}: {}", Bytecodes.getBytecodeName(lastCode), top());
//        lastCode = code;
        assert isAlive();
        resolvePendingJumps();
        buffer.put(code);
        pc++;
    }

    private void constant(Constant constant) {
        buffer.putShort(constPool.put(constant));
        pc += 2;
    }

    public void ldc(Object value) {
        code(LOAD_CONSTANT);
        var l = new LiteralValue(value);
        constant(l);
        state.push(l.getType().toStackType());
    }

    private void index(int index) {
        buffer.putShort(index);
        pc += 2;
    }

    public void index(int pc, int index) {
        buffer.put(pc, index >> 8);
        buffer.put(pc + 1, index);
    }

    public Executable getExecutable() {
        return executable;
    }

    public void loadContextSlot(int contextIndex, LocalVar v) {
        loadContextSlot(contextIndex, v.getIndex(), v.getType());
    }

    public void loadContextSlot(int contextIndex, int index, Type type) {
        code(LOAD_CONTEXT_SLOT);
        index(contextIndex);
        index(index);
        state.push(type);
    }

    public void storeContextSlot(int contextIndex, int index) {
        code(STORE_CONTEXT_SLOT);
        index(contextIndex);
        index(index);
        state.pop();
    }

    public void getStaticField(FieldRef field) {
        code(GET_STATIC_FIELD);
        constant(field);
        state.push(field.getType().toStackType());
    }

    public void setStatic(FieldRef field) {
        code(SET_STATIC);
        constant(field);
        state.pop();
    }

    public void setField(FieldRef field) {
        code(SET_FIELD);
        constant(field);
        state.pop(2);
    }

    public void cast(Type type) {
        var sourceType = state.peek();
        if (sourceType.isPrimitive() &&  type.isPrimitive())
            castPrim(sourceType.toStackType().getTag(), type.toStackType());
        else {
            code(CAST);
            constant( type);
            state.pop();
            state.push(type);
        }
    }

    public void castPrim(int sourceTag, Type targetType) {
        var sourceCode = sourceTag - TypeTags.TAG_INT;
        var targetCode = targetType.getTag() - TypeTags.TAG_INT;
        var offset = targetCode > sourceCode ? targetCode - 1 : sourceCode;
        var code = INT_TO_LONG + 3 * sourceCode + offset;
        code(code);
        state.pop();
        state.push(targetType);
    }

    public void mul(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_MUL;
            case TypeTags.TAG_LONG -> LONG_MUL;
            case TypeTags.TAG_FLOAT -> FLOAT_MUL;
            case TypeTags.TAG_DOUBLE -> DOUBLE_MUL;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void div(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_DIV;
            case TypeTags.TAG_LONG -> LONG_DIV;
            case TypeTags.TAG_FLOAT -> FLOAT_DIV;
            case TypeTags.TAG_DOUBLE -> DOUBLE_DIV;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void compare(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_COMPARE;
            case TypeTags.TAG_LONG -> LONG_COMPARE;
            case TypeTags.TAG_FLOAT -> FLOAT_COMPARE;
            case TypeTags.TAG_DOUBLE -> DOUBLE_COMPARE;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop(2);
        state.push(PrimitiveType.INT);
    }

    public void compareEq(Type type) {
        if (type.isPrimitive()) {
            compare(type);
            eq();
        } else {
            code(REF_COMPARE_EQ);
            state.pop(2);
            state.push(PrimitiveType.INT);
        }
    }

    public void compareNe(Type type) {
        if (type.isPrimitive()) {
            compare(type);
            ne();
        } else {
            code(REF_COMPARE_NE);
            state.pop(2);
            state.push(PrimitiveType.INT);
        }
    }

    public void add(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_ADD;
            case TypeTags.TAG_LONG -> LONG_ADD;
            case TypeTags.TAG_FLOAT -> FLOAT_ADD;
            case TypeTags.TAG_DOUBLE -> DOUBLE_ADD;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void sub(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_SUB;
            case TypeTags.TAG_LONG -> LONG_SUB;
            case TypeTags.TAG_FLOAT -> FLOAT_SUB;
            case TypeTags.TAG_DOUBLE -> DOUBLE_SUB;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void rem(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_REM;
            case TypeTags.TAG_LONG -> LONG_REM;
            case TypeTags.TAG_FLOAT -> FLOAT_REM;
            case TypeTags.TAG_DOUBLE -> DOUBLE_REM;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void inc(Type type) {
        switch (type.toStackType()) {
            case PrimitiveType.LONG -> {
                ldc(1L);
                code(LONG_ADD);
            }
            case PrimitiveType.INT -> {
                ldc(1);
                code(INT_ADD);
            }
            case PrimitiveType.FLOAT -> {
                ldc(1.0f);
                code(FLOAT_ADD);
            }
            case PrimitiveType.DOUBLE -> {
                ldc(1.0);
                code(DOUBLE_ADD);
            }
            default -> throw new RuntimeException("Cannot perform dec on value of type " + type.getTypeText());
        }
        state.pop();
    }

    public void dec(Type type) {
        switch (type.toStackType()) {
            case PrimitiveType.LONG -> {
                ldc(1L);
                code(LONG_SUB);
            }
            case PrimitiveType.INT -> {
                ldc(1);
                code(INT_SUB);
            }
            case PrimitiveType.FLOAT -> {
                ldc(1.0f);
                code(FLOAT_SUB);
            }
            case PrimitiveType.DOUBLE -> {
                ldc(1.0);
                code(DOUBLE_SUB);
            }
            default -> throw new RuntimeException("Cannot perform dec on value of type " + type.getTypeText());
        }
        state.pop();
    }

    public void gt() {
        code(GT);
    }

    public void lt() {
        code(LT);
    }

    public void ge() {
        code(GE);
    }

    public void le() {
        code(LE);
    }

    public void eq() {
        code(EQ);
    }

    public void ne() {
        code(NE);
    }

    public void bitAnd(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_BIT_AND;
            case TypeTags.TAG_LONG -> LONG_BIT_AND;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void bitOr(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_BIT_OR;
            case TypeTags.TAG_LONG -> LONG_BIT_OR;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void shl(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_SHIFT_LEFT;
            case TypeTags.TAG_LONG -> LONG_SHIFT_LEFT;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void shr(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_SHIFT_RIGHT;
            case TypeTags.TAG_LONG -> LONG_SHIFT_RIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void ushr(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_UNSIGNED_SHIFT_RIGHT;
            case TypeTags.TAG_LONG -> LONG_UNSIGNED_SHIFT_RIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void bitXor(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_INT -> INT_BIT_XOR;
            case TypeTags.TAG_LONG -> LONG_BIT_XOR;
            default -> throw new IllegalStateException("Unexpected value: " + type.getTag());
        };
        code(code);
        state.pop();
    }

    public void dup() {
        code(DUP);
        state.push(state.peek());
    }

    public void dupX1() {
        code(DUP_X1);
        var t1 = state.pop();
        var t2 = state.pop();
        state.push(t1);
        state.push(t2);
        state.push(t1);
    }

    public void dupX2() {
        code(DUP_X2);
        var t1 = state.pop();
        var t2 = state.pop();
        var t3 = state.pop();
        state.push(t1);
        state.push(t3);
        state.push(t2);
        state.push(t1);
    }

    public void arrayLoad() {
        code(GET_ELEMENT);
        state.pop();
        var arrayType = (ArrayType) state.pop();
        state.push(arrayType.getElementType());
    }

    public void arrayStore() {
        code(SET_ELEMENT);
        state.pop(3);
    }

    public void arrayLength() {
        code(ARRAY_LENGTH);
        state.pop();
        state.push(PrimitiveType.INT);
    }

    public void neg(Type type) {
        var code = switch (type.toStackType().getTag()) {
            case TypeTags.TAG_DOUBLE -> DOUBLE_NEG;
            case TypeTags.TAG_FLOAT -> FLOAT_NEG;
            case TypeTags.TAG_LONG -> LONG_NEG;
            case TypeTags.TAG_INT, TypeTags.TAG_SHORT, TypeTags.TAG_BYTE -> INT_NEG;
            default -> throw new RuntimeException("Cannot negate value of type: " + type.getTypeText());
        };
        code(code);
    }

    public void not() {
        code(NE);
    }

    public void bitNot(Type type) {
        switch (type.toStackType().getTag()) {
            case TypeTags.TAG_LONG -> {
                ldc(-1L);
                code(LONG_BIT_XOR);
            }
            case TypeTags.TAG_INT, TypeTags.TAG_SHORT, TypeTags.TAG_BYTE -> {
                ldc(-1);
                code(INT_BIT_XOR);
            }
            default -> throw new RuntimeException("Cannot negate value of type: " + type.getTypeText());
        }
    }

    public void nonnull() {
        code(NON_NULL);
        var t = state.pop();
        state.push(t.getUnderlyingType());
    }

    public void invokeFunction(FreeFuncRef func) {
        code(INVOKE_FUNCTION);
        constant(func);
        state.pop(func.getType().getParamTypes().size());
        pushReturnType(func.getRetType());
    }

    public void getMethod(MethodRef method) {
        assert !method.isStatic();
        code(GET_METHOD);
        constant(method);
        state.push(method.getType());
    }

    public void getStaticMethod(MethodRef method) {
        assert method.isStatic();
        code(GET_STATIC_METHOD);
        constant(method);
        state.push(method.getType());
    }

    public void write(MvOutput output) {
        output.writeInt(maxLocals);
        output.writeInt(maxStack);
        buffer.write(output);
    }

    public int length() {
        return buffer.length();
    }

    public void ret() {
        code(RETURN);
        alive = false;
    }

    public void voidRet() {
        code(VOID_RETURN);
        alive = false;
    }

    public Chain goto_(int offset) {
        return branch(GOTO, offset);
    }

    public void connect(Chain chain) {
        pendingJumps = Chain.merge(chain, pendingJumps);
    }

    private void resolvePendingJumps() {
        for (var c = pendingJumps; c != null; c = c.next()) {
            index(c.pc() + 1, pc - c.pc());
            if (Traces.traceGeneration)
                log.trace("Connecting branch {} to {}, branch top: {}", c.pc(), pc, c.state.top);
            if (alive)
                state.join(c.state());
            else {
                state = c.state();
                alive = true;
            }
        }
        pendingJumps = null;
    }

    public Chain branch(int opcode, int offset) {
        var pc = this.pc;
        code(opcode);
        index(offset);
        if (opcode != GOTO)
            state.pop();
        else
            alive = false;
        return new Chain(pc, state.copy(), null);
    }

    public void pop() {
        code(POP);
        state.pop();
    }

    public void raise() {
        code(RAISE);
        alive = false;
    }

    public int pc() {
        return pc;
    }

    public int top() {
        return state.top();
    }

    public boolean isAlive() {
        return alive || pendingJumps != null;
    }

    public void dup2() {
        code(DUP2);
        state.dup2();
    }

    public void lambda(Lambda lambda) {
        code(LAMBDA);
        constant(lambda);;
        false_();
        state.push(lambda.getType());
    }

    private void false_() {
        buffer.putBoolean(false);
        pc++;
    }

    public Type peek() {
        return state.peek();
    }

    public void newLocal(LocalVar variable) {
        if (Traces.traceGeneration)
            log.trace("New local: {}", variable.getName());
        variable.setIndex(nextLocal());
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }

    public int nextLocal() {
        return maxLocals++;
    }

    public void newArray(Type type) {
        code(NEW_ARRAY);
        constant(type);
        state.push(type);
    }

    public void arrayAdd() {
        code(ADD_ELEMENT);
        state.pop(2);
    }

    public class State {
        private Type[] stack;
        private int top;

        private State(Type[] stack, int top) {
            this.stack = stack;
            this.top = top;
        }

        public State() {
             stack = new Type[16];
        }

        public void push(Type type) {
            if (top >= stack.length)
                stack = Arrays.copyOf(stack, stack.length * 2);
            stack[top++] = type;
            if (top > maxStack)
                maxStack = top;
        }

        public void dup2() {
            var v1 = stack[top - 2];
            var v2 = stack[top - 1];
            push(v1);
            push(v2);
        }

        public Type pop() {
            var t = stack[--top];
            stack[top] = null;
            return t;
        }

        public void pop(int times) {
            for (int i = 0; i < times; i++) {
                pop();
            }
        }

        public Type peek() {
            return stack[top - 1];
        }

        public void join(State that) {
            Utils.require(top == that.top, "top not equal. current top: " + top + ", joined top: " + that.top);
            for (int i = 0; i < top; i++) {
                var type1 = stack[i];
                var type2 = that.stack[i];
                if (type1.isAssignableFrom(type2))
                    stack[i] = type1;
                else if (type2.isAssignableFrom(type1))
                    stack[i] = type2;
                else
                    stack[i] = Types.getLUB(List.of(type1, type2));
            }
        }

        public State copy() {
            return new State(Arrays.copyOf(stack, stack.length), top);
        }

        public int top() {
            return top;
        }

        public void printStack() {
            for (int i = 0; i < top; i++) {
                log.trace("{}", stack[i].getTypeText());
            }
        }

    }

    /** @noinspection unused*/
    public void printStack() {
        state.printStack();
    }

    public record Chain(int pc, State state, @Nullable Chain next) {

        public static Chain merge(Chain first, Chain second) {
            if (first == null && second == null)
                return null;
            if (first == null)
                return second;
            if (second == null)
                return first;
            return new Chain(first.pc, first.state, merge(first.next, second));
        }

    }

}
