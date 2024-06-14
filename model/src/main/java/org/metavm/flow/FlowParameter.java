package org.metavm.flow;

import org.metavm.object.type.TypeCategory;
import org.metavm.util.NamingUtils;

public class FlowParameter {
    private final TypeCategory type;
    private final long targetId;
    private final boolean multiValued;
    private final long id;
    private Object defaultValue;
    private String name;

    public FlowParameter(TypeCategory type,
                         long id,
                         String name,
                         long targetId,
                         boolean multiValued,
                         Object defaultValue) {
        this.type = type;
        this.id = id;
        this.targetId = targetId;
        this.multiValued = multiValued;
        setDefaultValue(defaultValue);
        setName(name);
    }

    public TypeCategory getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setName(String name) {
        this.name = NamingUtils.ensureValidName(name);
    }
}
