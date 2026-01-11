package org.manul.entity;

import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.InstanceVisitor;

public class StructuralTransformer extends InstanceVisitor<Element> {

    public Instance defaultValue(Instance instance) {
        return instance;
    }
}
