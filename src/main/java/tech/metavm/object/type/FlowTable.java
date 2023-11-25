package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.Flow;
import tech.metavm.util.NncUtils;

import java.util.IdentityHashMap;
import java.util.Map;

public class FlowTable {

    private final ClassType classType;
    private final Map<Flow, Flow> overriddenIndex = new IdentityHashMap<>();
    private final Map<Flow, Flow> verticalTemplateIndex = new IdentityHashMap<>();

    public FlowTable(ClassType classType) {
        this.classType = classType;
        rebuild();
    }

    public void rebuild() {
        verticalTemplateIndex.clear();
        overriddenIndex.clear();
        classType.foreachAncestor(t -> {
            for (Flow flow : t.getFlows()) {
                var override = overriddenIndex.putIfAbsent(flow, flow);
                if(override == null)
                    override = flow;
                for (Flow overridden : flow.getOverridden()) {
                    overriddenIndex.put(overridden, override);
                }
                var template = flow.getVerticalTemplate();
                if (template != null)
                    verticalTemplateIndex.put(template, flow);
            }
        });
    }

    public Flow findByVerticalTemplate(@NotNull Flow template) {
        return verticalTemplateIndex.get(template);
    }

    public Flow findByOverridden(Flow overridden) {
        return overriddenIndex.get(overridden);
    }

    public Flow lookup(Flow flowRef) {
        return NncUtils.requireNonNull(
                findByOverridden(flowRef), "Can not resolve flow " + flowRef + " in class " + classType);
    }

}
