package tech.metavm.management.persistence;

public class RegionPO {
    private int typeCategory;
    private long startId;
    private long endId;
    private long nextId;

    public RegionPO() {
    }

    public RegionPO(int typeCategory, long startId, long endId, long nextId) {
        this.typeCategory = typeCategory;
        this.startId = startId;
        this.endId = endId;
        this.nextId = nextId;
    }

    public int getTypeCategory() {
        return typeCategory;
    }

    public void setTypeCategory(int typeCategory) {
        this.typeCategory = typeCategory;
    }

    public long getNextId() {
        return nextId;
    }

    public void setNextId(long nextId) {
        this.nextId = nextId;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    public long getEndId() {
        return endId;
    }

    public void setEndId(long endId) {
        this.endId = endId;
    }
}
