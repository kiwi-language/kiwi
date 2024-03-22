package tech.metavm.object.instance.persistence;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record IndexQueryPO(long appId,
                           long constraintId,
                           List<IndexQueryItemPO> items,
                           boolean desc,
                           Long limit,
                           int lockMode) {

    public List<IndexEntryPO> execute(Collection<IndexEntryPO> indexItems) {
        return NncUtils.filterAndSortAndLimit(
                indexItems,
                this::matches,
                getItemComparator(),
                NncUtils.orElse(limit, Long.MAX_VALUE)
        );
    }

    public long count(Collection<IndexEntryPO> indexItems) {
        return indexItems.stream().filter(this::matches).count();
    }

    private Comparator<IndexEntryPO> getItemComparator() {
        if(desc)
            return (e1, e2) -> IndexKeyUtils.compare(e2.getKey(), e1.getKey());
        else
            return (e1, e2) -> IndexKeyUtils.compare(e1.getKey(), e2.getKey());
    }

    private boolean matches(IndexEntryPO indexItem) {
        if (appId != indexItem.getAppId() || constraintId != Id.fromBytes(indexItem.getIndexId()).getPhysicalId())
            return false;
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (!item.operator().evaluate(indexItem.getColumn(i), item.value()))
                return false;
        }
        return true;
    }

}
