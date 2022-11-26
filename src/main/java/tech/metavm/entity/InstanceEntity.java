//package tech.metavm.entity;
//
//import tech.metavm.object.instance.Instance;
//import tech.metavm.object.instance.InstanceContext;
//import tech.metavm.object.instance.persistence.VersionPO;
//import tech.metavm.object.instance.rest.InstanceDTO;
//import tech.metavm.object.instance.rest.InstanceFieldDTO;
//import tech.metavm.object.meta.Type;
//import tech.metavm.util.NncUtils;
//
//import java.util.List;
//
//public abstract class InstanceEntity extends Entity {
//
//    protected transient final long version;
//    protected transient final Instance instance;
//    protected final Type type;
//
//    public InstanceEntity(Instance instance) {
//        super(instance.getId(), instance.getContext());
//        this.instance = instance;
//        this.type = instance.getType();
//        this.version = instance.getVersion();
//        instance.addIdInitCallback(this::initId);
//    }
//
//    public InstanceEntity(Type type, List<InstanceFieldDTO> fields) {
//        this(type.newInstance(fields));
//    }
//
//    public VersionPO nextVersion() {
//        return new VersionPO(getTenantId(), id, version + 1);
//    }
//
//    public final void saveToContext() {
//        instance.update(toInstanceDTO());
//    }
//
//    final void removeFromContext() {
//        context.remove(this);
//    }
//
//    public Type getType() {
//        return type;
//    }
//
//    protected abstract InstanceDTO toInstanceDTO();
//
//}
