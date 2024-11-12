package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
public class MetaFrame implements Frame, CallContext, ClosureContext {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final int STATE_NEXT = 0;
    public static final int STATE_JUMP = 1;
    public static final int STATE_RET = 3;
    public static final int STATE_RAISE = 4;

    @Nullable
    private final ClassInstance self;
    private final List<? extends Value> arguments;
    @Nullable
    private final Klass owner;
    private final NodeRT entry;
    private final InstanceRepository instanceRepository;
    private final LinkedList<TryEnterNode> tryEnterNodes = new LinkedList<>();
    private ClassInstance exception;
    private Value returnValue;
    private NodeRT jumpTarget;
    private final Value[] locals;
    private final Value[] stack;
    private int top;
    @Nullable
    private final MetaFrame containingFrame;

    private final Map<TryEnterNode, ExceptionInfo> exceptions = new IdentityHashMap<>();

    public MetaFrame(@NotNull NodeRT entry,
                     @Nullable Klass owner,
                     @Nullable ClassInstance self,
                     List<? extends Value> arguments,
                     InstanceRepository instanceRepository,
                     @Nullable MetaFrame containingFrame,
                     int maxLocals,
                     int maxStack) {
        this.entry = entry;
        this.owner = owner;
        this.self = self;
        this.arguments = arguments;
        this.instanceRepository = instanceRepository;
        this.containingFrame = containingFrame;
        this.locals = new Value[maxLocals];
        this.stack = new Value[maxStack];
        if(self != null) {
            if(locals.length < arguments.size() + 1) {
                throw new IllegalStateException("Incorrect max locals for flow: " + entry.getFlow().getQualifiedName()
                        + ", maxLocals: " + entry.getFlow().getScope().getMaxLocals()
                        + ", template maxLocals: " + entry.getFlow().getEffectiveHorizontalTemplate().getScope().getMaxLocals());
            }
            locals[0] = self.getReference();
            for (int i = 0; i < arguments.size(); i++) {
                locals[i + 1] = arguments.get(i);
            }
        }
        else {
            if(locals.length < arguments.size()) {
                if(entry.getFlow() instanceof Method method) {
                    throw new IllegalStateException("Incorrect max locals for flow: " + entry.getFlow().getQualifiedName()
                            + ", maxLocals: " + entry.getFlow().getScope().getMaxLocals()
                            + ", horizontal template maxLocals: " + entry.getFlow().getEffectiveHorizontalTemplate().getScope().getMaxLocals()
                            + ", vertical template maxLocals: " + method.getEffectiveVerticalTemplate().getScope().getMaxLocals()
                    );
                }
                else {
                    throw new IllegalStateException("Incorrect max locals for flow: " + entry.getFlow().getQualifiedName()
                            + ", maxLocals: " + entry.getFlow().getScope().getMaxLocals()
                            + ", horizontal template maxLocals: " + entry.getFlow().getEffectiveHorizontalTemplate().getScope().getMaxLocals());
                }
            }
            for (int i = 0; i < arguments.size(); i++) {
                locals[i] = arguments.get(i);
            }
        }
    }

    public void addInstance(Instance instance) {
        instanceRepository.bind(instance);
    }

    public void enterTrySection(TryEnterNode tryEnterNode) {
        tryEnterNodes.push(tryEnterNode);
    }

    public TryEnterNode exitTrySection() {
        return tryEnterNodes.pop();
    }

    public int catchException(@NotNull NodeRT raiseNode, @NotNull ClassInstance exception) {
        var tryNode = tryEnterNodes.peek();
        if(tryNode != null) {
            exceptions.put(tryNode, new ExceptionInfo(raiseNode, exception));
            setJumpTarget(tryNode.getExit());
            return STATE_JUMP;
        }
        else {
            this.exception = exception;
            return STATE_RAISE;
        }
    }

    public @Nullable ExceptionInfo getExceptionInfo(TryEnterNode tryEnterNode) {
        return exceptions.get(tryEnterNode);
    }

    public void deleteInstance(Reference instance) {
        instanceRepository.remove(instance.resolve());
    }

    @SuppressWarnings("unused")
    public Object getInstanceField(ClassInstance instance, Field field) {
        checkAccess(field);
        return instance.getField(field);
    }

    private void checkAccess(Field field) {
        if (field.getAccess() == Access.PUBLIC)
            return;
        if (field.getAccess() == Access.PACKAGE)
            return;
        if (field.getAccess() == Access.PRIVATE) {
            if (!field.getDeclaringType().equals(owner))
                throw BusinessException.illegalAccess();
        }
    }

    public static final int MAX_STEPS = 100000;

    public @NotNull FlowExecResult execute() {
        var pc = entry;
        for(int i = 0; i < MAX_STEPS; i++) {
//            log.debug("Executing node {}, stack size: {}", pc.getName(), top);
            int state;
            try {
                state = pc.execute(this);
            }
            catch (Exception e) {
                throw new InternalException("Failed to execute node " + pc.getName()
                        + " in flow " + pc.getFlow().getQualifiedName(), e);
            }
            switch (state) {
                case STATE_NEXT -> pc = Objects.requireNonNull(pc.getSuccessor());
                case STATE_JUMP -> {
                    pc = Objects.requireNonNull(jumpTarget);
                    jumpTarget = null;
                }
                case STATE_RET -> {
                    return new FlowExecResult(returnValue, null);
                }
                case STATE_RAISE -> {
                    return new FlowExecResult(null, Objects.requireNonNull(exception));
                }
                default -> throw new IllegalStateException("Invalid execution state: " + state);
            }
        }
        throw new FlowExecutionException(String.format("Flow execution steps exceed the limit: %d", MAX_STEPS));
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

    public @Nullable ClassInstance getSelf() {
        return self;
    }

    public List<Value> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public ClassInstance getThrow() {
        return NncUtils.requireNonNull(exception);
    }

    @Override
    public InstanceRepository instanceRepository() {
        return instanceRepository;
    }

    public Value pop() {
        return stack[--top];
    }

    public Value peek() {
        return stack[top - 1];
    }

    public void push(@NotNull Value value) {
        stack[top++] = value;
    }

    public void load(int index) {
        push(locals[index]);
    }

    public void store(int index) {
        locals[index] = pop();
    }

    public ClosureContext getCapturingContext() {
        return Objects.requireNonNull(containingFrame);
    }

    public Value loadContextSlot(int contextIndex, int slotIndex) {
        return getCapturingContext().getContextSlot(contextIndex, slotIndex);
    }

    public void storeContextSlot(int contextIndex, int slotIndex) {
        getCapturingContext().setContextSlot(contextIndex, slotIndex, pop());
    }

    public Value getContextSlot(int contextIndex, int slotIndex) {
        assert contextIndex >= 0;
        if(contextIndex == 0)
            return locals[slotIndex];
        else
            return Objects.requireNonNull(containingFrame).getContextSlot(contextIndex - 1, slotIndex);
    }

    public void setContextSlot(int contextIndex, int slotIndex, Value value) {
        assert contextIndex >= 0;
        if(contextIndex == 0)
            locals[slotIndex] = value;
        else
            Objects.requireNonNull(containingFrame).setContextSlot(contextIndex - 1, slotIndex, value);
    }

    public void dup() {
        stack[top] = stack[top++ - 1];
    }

    public void dupX1() {
        var top = this.top;
        var v = stack[top] = stack[top-1];
        stack[top-1] = stack[top-2];
        stack[top-2] = v;
        this.top++;
    }

    public void dupX2() {
        var top = this.top;
        var v = stack[top] = stack[top-1];
        stack[top-1] = stack[top-2];
        stack[top-2] = stack[top-3];
        stack[top-3] = v;
        this.top++;
    }

    public Type getVariableType(int index) {
        return Objects.requireNonNull(locals[index], () -> "Variable " + index + " is not initialized").getType();
    }

    public IndexKeyRT loadIndexKey(Index index) {
        var values = new LinkedList<Value>();
        var numFields = index.getFields().size();
        for (int i = 0; i < numFields; i++) {
            values.addFirst(pop());
        }
        return index.createIndexKey(values);
    }

    public void setJumpTarget(NodeRT target) {
        this.jumpTarget = target;
    }

    public void setReturnValue(Value value) {
        returnValue = value;
    }

}
