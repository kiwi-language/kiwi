package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.natives.CallContext;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.NodeExpression;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Access;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
public class MetaFrame implements EvaluationContext, Frame, CallContext, ClosureContext {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @Nullable
    private final ClassInstance self;
    private final List<? extends Value> arguments;
    @Nullable
    private final Klass owner;
    private final Map<NodeRT, Value> outputs = new HashMap<>();
    private NodeRT entry;
    private final InstanceRepository instanceRepository;
    private FrameState state = FrameState.RUNNING;
    private final LinkedList<TryEnterNode> tryEnterNodes = new LinkedList<>();
    private ClassInstance exception;
    private final Value[] locals;
    private final Value[] stack;
    private int top;
    @Nullable
    private final MetaFrame containingFrame;

    private final Map<TryEnterNode, ExceptionInfo> exceptions = new IdentityHashMap<>();
    private NodeRT lastNode;

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
//        log.debug("flow: {}, maxLocals: {}, self: {}, arguments: {}",
//                entry.getFlow().getQualifiedName(), maxLocals, self, arguments.size());
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

    public Value getOutput(NodeRT node) {
        var result = outputs.get(node);
        if(result != null)
            return result;
        if(containingFrame != null)
            return containingFrame.getOutput(node);
        throw new IllegalStateException("Cannot find result for node " + node.getName()
                + " in flow " + node.getFlow().getQualifiedName());
    }

    public void setOutput(NodeRT node, Value output) {
        this.outputs.put(node, output);
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

    @SuppressWarnings("unused")
    public boolean inTrySection() {
        return !tryEnterNodes.isEmpty();
    }

    @SuppressWarnings("unused")
    public TryEnterNode currentTrySection() {
        return NncUtils.requireNonNull(tryEnterNodes.peek());
    }

    public NodeExecResult catchException(@NotNull NodeRT raiseNode, @NotNull ClassInstance exception) {
        var tryNode = tryEnterNodes.peek();
        if(tryNode != null) {
            exceptions.put(tryNode, new ExceptionInfo(raiseNode, exception));
            return NodeExecResult.jump(tryNode.getExit());
        }
        else
            return NodeExecResult.exception(exception);
    }

    @SuppressWarnings("unused")
    private void exception(ClassInstance exception, boolean fromResume) {
        if(!tryEnterNodes.isEmpty()) {
            var tryNode = tryEnterNodes.peek();
            exceptions.put(tryNode, new ExceptionInfo(entry, exception));
            if(fromResume) {
                entry = tryNode.getSuccessor();
            }
            else {
                jumpTo(tryNode.getSuccessor());
            }
        }
        else {
            state = FrameState.EXCEPTION;
            this.exception = exception;
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
        var outputs = this.outputs;
        var pc = entry;
        for(int i = 0; i < MAX_STEPS; i++) {
//            log.debug("Executing node {}", pc.getName());
            NodeExecResult result;
            try {
                result = pc.execute(this);
            }
            catch (Exception e) {
                throw new InternalException("Failed to execute node " + pc.getName()
                        + " in flow " + pc.getFlow().getQualifiedName(), e);
            }
            if(result.exception() != null)
                return new FlowExecResult(null, result.exception());
            var output = result.output();
            if(result.next() == null)
                return new FlowExecResult(output, null);
            if(output != null)
                outputs.put(pc, output);
            lastNode = pc;
            pc = result.next();
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

    @Override
    public Value evaluate(Expression expression) {
        if (expression instanceof NodeExpression nodeExpression) {
            return getOutput(nodeExpression.getNode());
        } else {
            throw new RuntimeException("context '" + this + "' doesn't support expression: " + expression);
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof NodeExpression;
    }

    public @Nullable ClassInstance getSelf() {
        return self;
    }

    public List<Value> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public List<Value> getLocals() {
        return List.of(locals);
    }

    public void jumpTo(NodeRT node) {
        this.entry = node;
    }

    public NodeRT getLastNode() {
        return lastNode;
    }

    public FrameState getState() {
        return state;
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

    public void push(Value value) {
        stack[top++] = value;
    }

    public Value load(int index) {
        return locals[index];
    }

    public void store(int index, Value value) {
        locals[index] = value;
    }

    public ClosureContext getCapturingContext() {
        return Objects.requireNonNull(containingFrame);
    }

    public Value loadContextSlot(int contextIndex, int slotIndex) {
        return getCapturingContext().getContextSlot(contextIndex, slotIndex);
    }

    public void storeContextSlot(int contextIndex, int slotIndex, Value value) {
        getCapturingContext().setContextSlot(contextIndex, slotIndex, value);
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

}
