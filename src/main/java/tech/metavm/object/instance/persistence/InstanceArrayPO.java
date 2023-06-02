package tech.metavm.object.instance.persistence;

import java.util.List;
import java.util.Map;

public final class InstanceArrayPO extends InstancePO {
    private Integer length;
    private List<Object> elements;
    private boolean elementAsChild;

    public InstanceArrayPO(
            Long id,
            long typeId,
            long tenantId,
            int length,
            List<Object> elements,
            boolean elementAsChild,
            long version,
            long syncVersion
    ) {
        super(tenantId, id, typeId, null, Map.of(), version, syncVersion);
        this.length = length;
        this.elements = elements;
        this.elementAsChild = elementAsChild;
    }

    public boolean isElementAsChild() {
        return elementAsChild;
    }

    public void setElementAsChild(boolean elementAsChild) {
        this.elementAsChild = elementAsChild;
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
                ", elementAsChild=" + elementAsChild +
                '}';
    }
}
