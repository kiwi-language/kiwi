package org.metavm.object.instance.core;

import org.metavm.flow.ClosureContext;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.rest.dto.InstanceParentRef;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public class ClassInstanceBuilder {

    public static ClassInstanceBuilder newBuilder(ClassType type) {
        return new ClassInstanceBuilder(type);
    }

    private Id id;
    private long version;
    private long syncVersion;
    private final ClassType type;
    private Map<Field, Value> data;
    private InstanceParentRef parentRef;
    private Consumer<Instance> load;
    private boolean ephemeral;
    private boolean initFieldTable = true;
    private @Nullable ClosureContext closureContext;

    private ClassInstanceBuilder(ClassType type) {
        this.type = type;
    }

    public ClassInstanceBuilder parentRef(InstanceParentRef parentRef) {
        this.parentRef = parentRef;
        return this;
    }

    public ClassInstanceBuilder data(Map<Field, Value> data) {
        this.data = data;
        return this;
    }

    public ClassInstanceBuilder id(Id id) {
        this.id = id;
        return this;
    }

    public ClassInstanceBuilder version(long version) {
        this.version = version;
        return this;
    }

    public ClassInstanceBuilder syncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
        return this;
    }

    public ClassInstanceBuilder load(Consumer<Instance> load) {
        this.load = load;
        return this;
    }

    public ClassInstanceBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ClassInstanceBuilder initFieldTable(boolean initFieldTable) {
        this.initFieldTable = initFieldTable;
        return this;
    }

    public ClassInstanceBuilder closureContext(ClosureContext closureContext) {
        this.closureContext = closureContext;
        return this;
    }

    public MvClassInstance build() {
        return new MvClassInstance(id, type, version, syncVersion, load, parentRef, data, ephemeral, initFieldTable, closureContext);
    }

    public Reference buildAndGetReference() {
        return build().getReference();
    }

}
