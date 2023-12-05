package tech.metavm.object.instance;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReferenceTree {


    private final int rootMode;

    private final Instance instance;

    private final List<ReferenceTree> children = new ArrayList<>();

    public ReferenceTree(Instance instance, int rootMode) {
        NncUtils.requireTrue(rootMode == 1 || rootMode == 2, "Invalid rootMode: " + rootMode);
        this.rootMode = rootMode;
        this.instance = instance;
    }

    void addChild(ReferenceTree child) {
        children.add(child);
    }

    public List<ReferenceTree> getChildren() {
        return children;
    }

    public List<String> getPaths() {
        List<String> result = new ArrayList<>();
        getPaths(result, new LinkedList<>());
        return result;
    }

    private boolean isRoot() {
        if (rootMode == 1) {
            if (instance instanceof ClassInstance classInstance) {
                if (classInstance.getType() == ModelDefRegistry.getClassType(ClassType.class)) {
                    var anonymousField = classInstance.getType().getFieldByJavaField(
                            ReflectUtils.getField(Type.class, "anonymous")
                    );
                    return ((BooleanInstance) classInstance.getField(anonymousField)).isFalse();
                }
            }
            return false;
        } else {
            return children.isEmpty();
        }
    }

    private void getPaths(List<String> result, LinkedList<String> path) {
        if (isRoot()) {
            StringBuilder buf = new StringBuilder(instance.getDescription());
            for (String s : path) {
                buf.append("->").append(s);
            }
            result.add(buf.toString());
        } else {
            path.addFirst(instance.getDescription());
            for (ReferenceTree child : children) {
                child.getPaths(result, path);
            }
            path.removeFirst();
        }
    }

}
