package org.metavm.autograph;

import org.metavm.util.ReflectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class StateStack {

    private final Map<Class<?>, LinkedList<State>> stacks = new HashMap<>();

    public <T extends State> T create(Class<T> klass) {
        var stack = stack(klass);
        var constructor = ReflectionUtils.getConstructor(klass, State.class, StateStack.class);
        T state = ReflectionUtils.newInstance(constructor, stack.peek(), this);
        stack.push(state);
        return state;
    }

    public <T extends State> T get(Class<T> klass) {
        return Objects.requireNonNull(stack(klass).peek());
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
