package org.metavm.system;

import com.sun.jdi.InternalException;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.TypeId;
import org.metavm.system.rest.dto.BlockDTO;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public class BlockRT implements Comparable<BlockRT> {
    private final long appId;
    private final long id;
    private final TypeId typeId;
    private final long start;
    private final long end;
    private boolean active;
    private long next;

    public BlockRT(long id, long appId, TypeId typeId, long start, long end, long next) {
        this.id = id;
        this.appId = appId;
        this.typeId = typeId;
        this.start = start;
        this.end = end;
        this.next = next;
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

    public TypeId getTypeId() {
        return typeId;
    }

    public int getSize() {
        return (int) (end - start);
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

    public long getNext() {
        return next;
    }

    public boolean isActive() {
        return active;
    }

    public long getAppId() {
        return appId;
    }

    public boolean contains(long id) {
        return id >= start && id < end;
    }

    public BlockDTO toDTO() {
        return new BlockDTO(id, appId, typeId.tag().code(), typeId.id(), start, end, next, active);
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
