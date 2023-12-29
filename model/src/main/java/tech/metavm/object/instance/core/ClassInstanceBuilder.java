package tech.metavm.object.instance.core;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.rest.dto.InstanceParentRef;

import java.util.Map;
import java.util.function.Consumer;

public class ClassInstanceBuilder {

    public static ClassInstanceBuilder newBuilder(ClassType type) {
        return new ClassInstanceBuilder(type);
    }

    private Long id;
    private long version;
    private long syncVersion;
    private ClassInstance source;
    private final ClassType type;
    private Map<Field, Instance> data;
    private InstanceParentRef parentRef;
    private Consumer<DurableInstance> load;
    private Long tmpId;

    private ClassInstanceBuilder(ClassType type) {
        this.type = type;
    }

    public ClassInstanceBuilder parentRef(InstanceParentRef parentRef) {
        this.parentRef = parentRef;
        return this;
    }

    public ClassInstanceBuilder data(Map<Field, Instance> data) {
        this.data = data;
        return this;
    }

    public ClassInstanceBuilder source(ClassInstance source) {
        this.source = source;
        return this;
    }

    public ClassInstanceBuilder id(Long id) {
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

    public ClassInstanceBuilder load(Consumer<DurableInstance> load) {
        this.load = load;
        return this;
    }

    public ClassInstanceBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public ClassInstance build() {
        var instance =  new ClassInstance(id, type, version, syncVersion, load, parentRef, data, source);
        if(tmpId != null)
            instance.setTmpId(tmpId);
        return instance;
    }

}
