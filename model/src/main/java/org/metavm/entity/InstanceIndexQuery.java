package org.metavm.entity;

import org.metavm.object.instance.core.InstanceIndexKey;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Index;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public record InstanceIndexQuery(
        Index index,
        @Nullable InstanceIndexKey from,
        @Nullable InstanceIndexKey to,
        boolean desc,
        @Nullable Long limit) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstanceIndexQuery) obj;
        return Objects.equals(this.index, that.index) &&
                Objects.equals(this.from, that.to) &&
                Objects.equals(this.to, that.to) &&
                this.desc == that.desc &&
                Objects.equals(this.limit, that.limit);
    }

    public boolean memoryOnly() {
        return index.isIdNull()
                || from != null && NncUtils.anyMatch(from.values(), i -> i instanceof Reference d && !d.isIdInitialized())
                || to != null && NncUtils.anyMatch(to.values(), i -> i instanceof Reference d && !d.isIdInitialized());
    }

    @Override
    public String toString() {
        return "InstanceIndexQuery[" +
                "index=" + index + ", " +
                "from=" + from + ", " +
                "to=" + to + ", " +
                "desc=" + desc + ", " +
                "limit=" + limit + ']';
    }


}
