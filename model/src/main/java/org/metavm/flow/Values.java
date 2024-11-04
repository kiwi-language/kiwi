package org.metavm.flow;

import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Property;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;

import java.util.List;

public class Values {

    public static Value constantLong(long value) {
        return constant(Instances.longInstance(value));
    }

    public static Value never() {
        return new NeverValue();
    }

    public static Value constantString(String string) {
        return constant(Instances.stringInstance(string));
    }

    public static Value constantBoolean(boolean bool) {
        return constant(Instances.booleanInstance(bool));
    }

    public static Value constant(org.metavm.object.instance.core.Value value) {
        return new ConstantValue(value);
    }

    public static Value constantTrue() {
        return constant(Instances.trueInstance());
    }

    public static Value constantFalse() {
        return constant(Instances.falseInstance());
    }

    public static Value nullValue() {
        return new ConstantValue(Instances.nullInstance());
    }

    public static Value node(NodeRT node) {
        return new NodeValue(node);
    }

    public static Value type(Type type) {
        return new TypeValue(type);
    }

    public static Value getOrNever(Value value) {
        return value != null ? value : never();
    }

    public static Value constantDouble(double v) {
        return new ConstantValue(Instances.doubleInstance(v));
    }

    public static Value array(List<Value> elements, ArrayType type) {
        return new ArrayValue(elements, type);
    }

    public static Value constantChar(char c) {
        return constant(Instances.charInstance(c));
    }

    public static Value property(Property property) {
        return new PropertyValue(property.getRef());
    }
}
