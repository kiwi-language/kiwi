package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.meta.Field;

import java.util.Objects;

public class InstanceRelation {
    private final Instance source;
    private final InstanceField field;
    private long destinationId;

    public InstanceRelation(Instance source, InstanceField field, long destinationId) {
        this.source = source;
        this.field = field;
        this.destinationId = destinationId;
    }

    public InstanceField getField() {
        return field;
    }

    public String getDestKey() {
        return field.getId() + "-" + destinationId;
    }

    public Instance getSource() {
        return source;
    }

    public Long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    public RelationPO toPO() {
        return new RelationPO(source.getTenantId(), field.getId(), source.getId(), destinationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceRelation that = (InstanceRelation) o;
        return destinationId == that.destinationId && Objects.equals(source, that.source) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, field, destinationId);
    }
}
