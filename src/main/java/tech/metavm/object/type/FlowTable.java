package tech.metavm.object.type;

import tech.metavm.flow.Flow;
import tech.metavm.util.NncUtils;

import java.util.IdentityHashMap;
import java.util.Map;

public class FlowTable {

    private final ClassType classType;
    private final Map<Flow, Flow> map = new IdentityHashMap<>();

    public FlowTable(ClassType classType) {
        this.classType = classType;
        rebuild();
    }

    public void rebuild() {
        map.clear();
        var type = classType;
        while (type != null) {
            for (Flow flow : type.getFlows()) {
                if(!map.containsKey(flow)) {
                    map.put(flow, flow);
                    for (Flow overriden : flow.getAllOverriden()) {
                        map.put(overriden, flow);
                    }
                }
            }
            type = type.getSuperClass();
        }
    }

    public Flow tryLookup(Flow flowRef) {
        return map.get(flowRef);
    }

    public Flow lookup(Flow flowRef) {
        return NncUtils.requireNonNull(
                tryLookup(flowRef), "Can not resolve flow " + flowRef + " in class " + classType);
    }

}
