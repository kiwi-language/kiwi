package tech.metavm.object.instance.core;

import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.rest.dto.InstanceParentRef;

import java.util.Map;
import java.util.function.Consumer;

public class ClassInstanceBuilder {

    public static ClassInstanceBuilder newBuilder(Klass type) {
        return new ClassInstanceBuilder(type);
    }

    private Id id;
    private long version;
    private long syncVersion;
    private SourceRef sourceRef;
    private final Klass type;
    private Map<Field, Instance> data;
    private InstanceParentRef parentRef;
    private Consumer<DurableInstance> load;
    private boolean ephemeral;
//    private Long tmpId;

    private ClassInstanceBuilder(Klass type) {
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

    public ClassInstanceBuilder sourceRef(SourceRef sourceRef) {
        this.sourceRef = sourceRef;
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

    public ClassInstanceBuilder load(Consumer<DurableInstance> load) {
        this.load = load;
        return this;
    }

//    public ClassInstanceBuilder tmpId(Long tmpId) {
//        this.tmpId = tmpId;
//        return this;
//    }

    public ClassInstanceBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ClassInstance build() {
        //        if(tmpId != null)
//            instance.setTmpId(tmpId);
        return new ClassInstance(id, type, version, syncVersion, load, parentRef, data, sourceRef, ephemeral);
    }

}
