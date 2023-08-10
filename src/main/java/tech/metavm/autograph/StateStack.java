package tech.metavm.autograph;

import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class StateStack {

    private final Map<Class<?>, LinkedList<State>> stacks = new HashMap<>();

    public <T extends State> T create(Class<T> klass) {
        var stack = stack(klass);
        var constructor = ReflectUtils.getConstructor(klass, State.class, StateStack.class);
        T state = ReflectUtils.newInstance(constructor, stack.peek(), this);
        stack.push(state);
        return state;
    }

    public <T extends State> T get(Class<T> klass) {
        return NncUtils.requireNonNull(stack(klass).peek());
    }

    public boolean has(Class<?> klass) {
        return !stack(klass).isEmpty();
    }

    public <T extends State> void pop(Class<T> klass) {
        stack(klass).pop();
    }

    private <T> LinkedList<T> stack(Class<T> klass) {
        //noinspection unchecked
        return (LinkedList<T>) stacks.computeIfAbsent(klass, k -> new LinkedList<>());
    }

}
