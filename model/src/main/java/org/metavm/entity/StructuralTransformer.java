package org.metavm.entity;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;

public class StructuralTransformer extends InstanceVisitor<Element> {

    public Instance defaultValue(Instance instance) {
        return instance;
    }
}
