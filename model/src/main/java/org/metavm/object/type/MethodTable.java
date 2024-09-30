package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.flow.Method;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MethodTable {

    private final Klass classType;
    private final Map<Method, Method> overriddenIndex = new IdentityHashMap<>();
    private final Map<Method, Method> verticalTemplateIndex = new IdentityHashMap<>();
    private @Nullable Method hashCodeMethod;
    private @Nullable Method equalsMethod;
    private @Nullable Method toStringMethod;

    public MethodTable(Klass classType) {
        this.classType = classType;
        rebuild();
    }

    public void rebuild() {
        hashCodeMethod = classType.findMethod(m -> "hashCode".equals(m.getCode()) && m.getParameters().isEmpty());
        equalsMethod = classType.findMethod(m -> "equals".equals(m.getCode()) && m.getParameterTypes().equals(List.of(Types.getNullableAnyType())));
        toStringMethod = classType.findMethod(m -> "toString".equals(m.getCode()) && m.getParameters().isEmpty());
        verticalTemplateIndex.clear();
        overriddenIndex.clear();
        classType.foreachAncestor(t -> {
            for (Method method : t.getMethods()) {
                var override = overriddenIndex.putIfAbsent(method, method);
                if(override == null)
                    override = method;
                for (Method overridden : method.getOverridden()) {
                    overriddenIndex.put(overridden, override);
                }
                var template = method.getVerticalTemplate();
                if (template != null)
                    verticalTemplateIndex.put(template, method);
            }
        });
    }

    public Method findByVerticalTemplate(@NotNull Method template) {
        return verticalTemplateIndex.get(template);
    }

    public Method findByOverridden(Method overridden) {
        return overriddenIndex.get(overridden);
    }

    public Method lookup(Method methodRef) {
        return NncUtils.requireNonNull(
                findByOverridden(methodRef), "Can not resolve method " + methodRef + " in class " + classType);
    }

    public @Nullable Method getHashCodeMethod() {
        return hashCodeMethod;
    }

    public @Nullable Method getEqualsMethod() {
        return equalsMethod;
    }

    public @Nullable Method getToStringMethod() {
        return toStringMethod;
    }
}
