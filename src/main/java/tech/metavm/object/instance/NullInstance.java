package tech.metavm.object.instance;

import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.util.Null;

public class NullInstance extends PrimitiveInstance {

    public NullInstance(PrimitiveType type) {
        super(type);
    }

    @Override
    public Null getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "NullInstance null:" + getType().getName();
    }

    @Override
    public String getTitle() {
        return "空";
    }
}
