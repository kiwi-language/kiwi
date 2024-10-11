package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.flow.Method;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
public class MethodTable {

    private final Klass classType;
    private final Map<Method, Method> overriddenIndex = new IdentityHashMap<>();
    private final Map<Method, Method> verticalTemplateIndex = new IdentityHashMap<>();
    private @Nullable Method hashCodeMethod;
    private @Nullable Method equalsMethod;
    private @Nullable Method toStringMethod;
    private @Nullable Method writeObjectMethod;
    private @Nullable Method readObjectMethod;

    public MethodTable(Klass classType) {
        this.classType = classType;
        rebuild();
    }

    public void rebuild() {
        hashCodeMethod = classType.findMethod(m -> "hashCode".equals(m.getCode()) && m.getParameters().isEmpty());
        equalsMethod = classType.findMethod(m -> "equals".equals(m.getCode()) && m.getParameterTypes().equals(List.of(Types.getNullableAnyType())));
        toStringMethod = classType.findMethod(m -> "toString".equals(m.getCode()) && m.getParameters().isEmpty());
        writeObjectMethod = classType.findSelfMethod(m -> "writeObject".equals(m.getCode())
                && m.getParameters().size() == 1);
        readObjectMethod = classType.findSelfMethod(m -> "readObject".equals(m.getCode())
                && m.getParameters().size() == 1);
        verticalTemplateIndex.clear();
        overriddenIndex.clear();
        var sig2methods = new HashMap<SimpleSignature, List<Method>>();
        classType.forEachSuperClass(s -> {
            for (Method method : s.getMethods()) {
                if(method.isVirtual() && !method.isAbstract())
                    sig2methods.computeIfAbsent(SimpleSignature.of(method), k -> new ArrayList<>()).add(method);
            }
        });
        classType.foreachAncestor(s -> {
            if(s.isInterface()) {
                for (Method method : s.getMethods()) {
                    if (method.isVirtual() && !method.isAbstract())
                        sig2methods.computeIfAbsent(SimpleSignature.of(method), k -> new ArrayList<>()).add(method);
                }
            }
        });
        classType.forEachMethod(method -> {
            if(method.isVirtual()) {
                var override = NncUtils.find(sig2methods.get(SimpleSignature.of(method)), m -> m.isOverrideOf(method));
                overriddenIndex.put(method, Objects.requireNonNullElse(override, method));
            }
        });
        classType.foreachAncestor(t -> {
            for (Method method : t.getMethods()) {
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

    @Nullable
    public Method getWriteObjectMethod() {
        return writeObjectMethod;
    }

    @Nullable
    public Method getReadObjectMethod() {
        return readObjectMethod;
    }

    private record SimpleSignature(
            String name,
            int parameterCount,
            int typeParameterCount
    ) {

        static SimpleSignature of(Method method) {
            return new SimpleSignature(
                    method.getName(),
                    method.getParameters().size(),
                    method.getTypeParameters().size()
            );
        }

    }

}
