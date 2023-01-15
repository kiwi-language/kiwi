package tech.metavm.entity;

import tech.metavm.object.instance.persistence.IndexQueryPO;
import tech.metavm.object.meta.IndexField;
import tech.metavm.object.meta.Index;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class InstanceIndexQuery {
    private final Index index;
    private final List<InstanceIndexQueryItem> items;
    private final IndexQueryOperator lastOperator;
    private final boolean desc;
    private final long limit;

    public InstanceIndexQuery(
            Index index,
            List<InstanceIndexQueryItem> items,
            IndexQueryOperator lastOperator,
            boolean desc,
            long limit
    ) {
        IndexField lastIndexItem = items.get(items.size() - 1).indexItem();
        if(!lastIndexItem.isColumnX() && lastOperator != IndexQueryOperator.EQ) {
            throw new InternalException("lastOperator can only be EQ when the last term is not a long value");
        }
        this.index = index;
        this.items = items;
        this.lastOperator = lastOperator;
        this.desc = desc;
        this.limit = limit;
    }

    public IndexQueryPO toPO(long tenantId) {
        return new IndexQueryPO(
                tenantId,
                index.getId(),
                index.createIndexKey(NncUtils.map(items, InstanceIndexQueryItem::value)),
                lastOperator,
                desc,
                limit
        );
    }

    public Index index() {
        return index;
    }

    public List<InstanceIndexQueryItem> items() {
        return items;
    }

    public IndexQueryOperator lastOperator() {
        return lastOperator;
    }

    public boolean desc() {
        return desc;
    }

    public long limit() {
        return limit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstanceIndexQuery) obj;
        return Objects.equals(this.index, that.index) &&
                Objects.equals(this.items, that.items) &&
                Objects.equals(this.lastOperator, that.lastOperator) &&
                this.desc == that.desc &&
                this.limit == that.limit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, items, lastOperator, desc, limit);
    }

    @Override
    public String toString() {
        return "InstanceIndexQuery[" +
                "index=" + index + ", " +
                "items=" + items + ", " +
                "lastOperator=" + lastOperator + ", " +
                "desc=" + desc + ", " +
                "limit=" + limit + ']';
    }


}
