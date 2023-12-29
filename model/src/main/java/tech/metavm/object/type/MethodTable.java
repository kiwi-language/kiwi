package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.Method;
import tech.metavm.util.NncUtils;

import java.util.IdentityHashMap;
import java.util.Map;

public class MethodTable {

    private final ClassType classType;
    private final Map<Method, Method> overriddenIndex = new IdentityHashMap<>();
    private final Map<Method, Method> verticalTemplateIndex = new IdentityHashMap<>();

    public MethodTable(ClassType classType) {
        this.classType = classType;
        rebuild();
    }

    public void rebuild() {
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

}
