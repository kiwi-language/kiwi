package tech.metavm.infra.persistence;

import tech.metavm.entity.Identifiable;

public class BlockPO implements Identifiable {
    private Long id;
    private Long tenantId;
    private Long typeId;
    private Long start;
    private Long end;
    private Long next;
    private Boolean active;

    public BlockPO() {}

    public BlockPO(Long id, Long tenantId, Long typeId, Long start, Long end, Long nextId, Boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.typeId = typeId;
        this.start = start;
        this.end = end;
        this.next = nextId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getNext() {
        return next;
    }

    public void setNext(Long next) {
        this.next = next;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "IdRange{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", typeId=" + typeId +
                ", start=" + start +
                ", end=" + end +
                ", next=" + next +
                ", active=" + active +
                '}';
    }
}