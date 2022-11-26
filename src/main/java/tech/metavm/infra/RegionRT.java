package tech.metavm.infra;

import tech.metavm.infra.persistence.RegionPO;
import tech.metavm.object.meta.TypeCategory;

public class RegionRT {

    private final TypeCategory typeCategory;
    private final long start;
    private final long end;
    private long next;

    public RegionRT(RegionPO regionPO) {
        this(
                TypeCategory.getByCodeRequired(regionPO.getTypeCategory()),
                regionPO.getStart(),
                regionPO.getEnd(),
                regionPO.getNext()
        );
    }

    public RegionRT(TypeCategory typeCategory, long start, long end, long next) {
        this.typeCategory = typeCategory;
        this.start = start;
        this.end = end;
        this.next = next;
    }

    public TypeCategory getTypeCategory() {
        return typeCategory;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public boolean contains(long id) {
        return id >= start && id < end;
    }

    public RegionPO toPO() {
        return new RegionPO(
                typeCategory.code(),
                start,
                end,
                next
        );
    }

}
