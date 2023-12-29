package tech.metavm.object.instance.core;

public interface InstanceProvider {

    DurableInstance get(Id id);

    default DurableInstance get(long id) {
        return get(PhysicalId.of(id));
    }

}
