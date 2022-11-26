package tech.metavm.infra.persistence;

public class RegionPO {
    private int typeCategory;
    private long start;
    private long end;
    private long next;

    public RegionPO() {
    }

    public RegionPO(int typeCategory, long start, long end, long nextId) {
        this.typeCategory = typeCategory;
        this.start = start;
        this.end = end;
        this.next = nextId;
    }

    public int getTypeCategory() {
        return typeCategory;
    }

    public void setTypeCategory(int typeCategory) {
        this.typeCategory = typeCategory;
    }

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
