package tech.metavm.object.instance.persistence;

import java.util.Objects;

public class IdSetNodePO {

    private Long setId;
    private Long value;

    public IdSetNodePO() {}

    public IdSetNodePO(Long setId, Long value) {
        this.setId = setId;
        this.value = value;
    }

    public Long getSetId() {
        return setId;
    }

    public void setSetId(Long setId) {
        this.setId = setId;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdSetNodePO that = (IdSetNodePO) o;
        return Objects.equals(setId, that.setId) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(setId, value);
    }
}