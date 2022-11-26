package tech.metavm.object.instance.persistence;

import java.util.List;
import java.util.Map;

public final class InstanceArrayPO extends InstancePO {
    private Integer length;
    private List<Long> elementIds;
    private boolean elementAsChild;

    public InstanceArrayPO() {
    }

    public InstanceArrayPO(
            long id,
            long typeId,
            long tenantId,
            int length,
            List<Long> elementIds,
            boolean elementAsChild,
            long version,
            long syncVersion
    ) {
        super(tenantId, id, typeId, null, Map.of(), version, syncVersion);
        this.length = length;
        this.elementIds = elementIds;
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

    public List<Long> getElementIds() {
        return elementIds;
    }

    public void setElementIds(List<Long> elementIds) {
        this.elementIds = elementIds;
    }

}
