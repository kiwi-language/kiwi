package tech.metavm.object.instance;

import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.Value;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.meta.Field;

import java.util.Objects;

import static tech.metavm.util.ContextUtil.getTenantId;

public class InstanceRelation extends Value {
    private final IInstance source;
    private final Field field;
    private final IInstance destination;

    public InstanceRelation(IInstance source, Field field, IInstance destination) {
        super(true/*, context*/);
        this.source = source;
        this.field = field;
        this.destination = destination;
    }

//    public InstanceRelation(IInstance source, Field field, IInstance destination) {
//        super(false/*, source.getContext()*/);
//        this.source = source;
//        this.field = field;
//        this.destination = destination;
//    }

    public Field getField() {
        return field;
    }

    public long getFieldId() {
        return field.getId();
    }

    public String getDestKey() {
        return field.getId() + "-" + destination.getId();
    }

    public IInstance getSource() {
        return source;
    }

    public RelationPO toPO() {
        return new RelationPO(getTenantId(), field.getId(), source.getId(), destination.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceRelation that = (InstanceRelation) o;
        return Objects.equals(source, that.source) && Objects.equals(field, that.field) && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, field, destination);
    }

    public long getDestInstanceId() {

        return destination.getId();
    }
}
