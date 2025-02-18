package org.metavm.object.instance.core;

import org.metavm.flow.ClosureContext;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public class ClassInstanceBuilder {

    public static ClassInstanceBuilder newBuilder(ClassType type, Id id) {
        return new ClassInstanceBuilder(type, id);
    }

    private final Id id;
    private long version;
    private long syncVersion;
    private final ClassType type;
    private Map<Field, ? extends Value> data;
    private @Nullable ClassInstance parent;
    private boolean ephemeral;
    private boolean initFieldTable = true;
    private boolean isNew;
    private @Nullable ClosureContext closureContext;

    private ClassInstanceBuilder(ClassType type, Id id) {
        this.type = type;
        this.id = id;
        isNew = !type.isValueType() && !type.isEphemeral();
    }

    public ClassInstanceBuilder parent(ClassInstance parent) {
        this.parent = parent;
        return this;
    }

    public ClassInstanceBuilder data(Map<Field, ? extends Value> data) {
        this.data = data;
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

    public ClassInstanceBuilder isNew(boolean isNew) {
        if (!type.isEphemeral() && !type.isValueType() && !ephemeral)
            this.isNew = isNew;
        return this;
    }

    public ClassInstanceBuilder closureContext(ClosureContext closureContext) {
        this.closureContext = closureContext;
        return this;
    }

    public MvClassInstance build() {
        return new MvClassInstance(id, type, version, syncVersion, parent, data, ephemeral, initFieldTable, isNew, closureContext);
    }

    public Reference buildAndGetReference() {
        return build().getReference();
    }

}
