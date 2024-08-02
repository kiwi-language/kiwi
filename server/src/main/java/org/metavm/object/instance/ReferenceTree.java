package org.metavm.object.instance;

import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

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
                if (classInstance.getType() == ModelDefRegistry.getClassType(Klass.class)) {
                    var anonymousField = classInstance.getKlass().getFieldByJavaField(
                            ReflectionUtils.getField(Type.class, "anonymous")
                    );
                    return ((BooleanValue) classInstance.getField(anonymousField)).isFalse();
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
