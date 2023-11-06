package tech.metavm.object.instance.persistence;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class InstanceArrayPO extends InstancePO {
    private Integer length;
    private List<Object> elements;

    public InstanceArrayPO() {
    }

    public InstanceArrayPO(
            Long id,
            long typeId,
            long tenantId,
            int length,
            List<Object> elements,
            @Nullable Long parentId,
            @Nullable Long parentFieldId,
            long version,
            long syncVersion
    ) {
        super(tenantId, id, typeId, null, Map.of(), parentId, parentFieldId, version, syncVersion);
        this.length = length;
        this.elements = elements;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public List<Object> getElements() {
        return elements;
    }

    public void setElements(List<Object> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "InstanceArrayPO{" +
                "id=" + getId() +
                ", length=" + length +
                ", elementIds=" + elements +
                '}';
    }
}
