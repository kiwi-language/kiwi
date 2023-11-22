package tech.metavm.management;

import com.sun.jdi.InternalException;
import org.jetbrains.annotations.NotNull;
import tech.metavm.management.persistence.BlockPO;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public class BlockRT implements Comparable<BlockRT> {
    private final long tenantId;
    private final long id;
    private final long typeId;
    private final long start;
    private final long end;
    private boolean active;
    private long next;

    public BlockRT(BlockPO idRange) {
        this.tenantId = idRange.getTenantId();
        this.id = idRange.getId();
        this.typeId = idRange.getTypeId();
        this.start = idRange.getStartId();
        this.end = idRange.getEndId();
        this.next = idRange.getNextId();
        this.active = idRange.getActive();
    }

    public BlockRT(long id, long tenantId, long typeId, long start, long end) {
        this.id = id;
        this.tenantId = tenantId;
        this.typeId = typeId;
        this.start = start;
        this.end = end;
        this.next = start;
        this.active = true;
    }

    public List<Long> allocate(long count) {
        if(count > available()) {
            throw new InternalException("Not enough ids available in the block");
        }
        List<Long> results = NncUtils.range(next, next + count);
        next += count;
        if(available() == 0) {
            active = false;
        }
        return results;
    }

    public int available() {
        return (int) (end - next);
    }

    public long getTypeId() {
        return typeId;
    }

    public int getSize() {
        return (int) (end - start);
    }

    public BlockPO toPO() {
        return new BlockPO(
                id,
                tenantId,
                typeId,
                start,
                end,
                next,
                active
        );
    }

    public long getStart() {
        return start;
    }

    public long getId() {
        return id;
    }

    public long getEnd() {
        return end;
    }

    public long getTenantId() {
        return tenantId;
    }

    public boolean contains(long id) {
        return id >= start && id < end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockRT blockRT = (BlockRT) o;
        return id == blockRT.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull BlockRT o) {
        return Long.compare(start, o.start);
    }

}
