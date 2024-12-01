package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNullElse;

@Slf4j
public class MethodTable {

    private final Klass klass;
    private final ConstantPool constantPool;
    private final Map<Method, Integer> overriddenIndex = new HashMap<>();
    private @Nullable Method hashCodeMethod;
    private @Nullable Method equalsMethod;
    private @Nullable Method toStringMethod;
    private @Nullable Method writeObjectMethod;
    private @Nullable Method readObjectMethod;

    public MethodTable(Klass klass) {
        this.klass = klass;
        constantPool = klass.getConstantPool();
        rebuild();
    }

    public void rebuild() {
        hashCodeMethod = klass.findMethod(m -> "hashCode".equals(m.getName()) && m.getParameters().isEmpty());
        equalsMethod = klass.findMethod(m -> "equals".equals(m.getName()) && m.getParameterTypes().equals(List.of(Types.getNullableAnyType())));
        toStringMethod = klass.findMethod(m -> "toString".equals(m.getName()) && m.getParameters().isEmpty());
        writeObjectMethod = klass.findSelfMethod(m -> "writeObject".equals(m.getName())
                && m.getParameters().size() == 1);
        readObjectMethod = klass.findSelfMethod(m -> "readObject".equals(m.getName())
                && m.getParameters().size() == 1);
        overriddenIndex.clear();
        var sig2methods = new HashMap<SimpleSignature, List<MethodRef>>();
        var type = klass.getType();
        type.foreachSuperClass(s -> {
            s.foreachSelfMethod(m -> {
                if(m.isVirtual() && !m.isAbstract())
                    sig2methods.computeIfAbsent(SimpleSignature.of(m.getRawFlow()), k -> new ArrayList<>()).add(m);
            });
        });
        type.foreachAncestor(s -> {
            if(s.isInterface()) {
                s.foreachSelfMethod(m -> {
                    if (m.isVirtual() && !m.isAbstract())
                        sig2methods.computeIfAbsent(SimpleSignature.of(m.getRawFlow()), k -> new ArrayList<>()).add(m);
                });
            }
        });
        type.foreachMethod((method) -> {
            if(method.isVirtual()) {
                var override = NncUtils.find(sig2methods.get(SimpleSignature.of(method.getRawFlow())), m -> m.isOverrideOf(method));
                overriddenIndex.put(method.getRawFlow(), constantPool.addValue(requireNonNullElse(override, method)));
            }
        });
    }

    public MethodRef findOverride(Method overridden, TypeMetadata typeMetadata) {
        var i = overriddenIndex.get(overridden);
        return i != null ? typeMetadata.getMethodRef(i) : null;
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

    public Map<Method, MethodRef> getOverriddenIndex(TypeMetadata typeMetadata) {
        var m = new HashMap<Method, MethodRef>();
        overriddenIndex.forEach((method, idx) -> m.put(method, typeMetadata.getMethodRef(idx)));
        return m;
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
