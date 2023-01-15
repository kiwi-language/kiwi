package tech.metavm.object.instance.persistence;

import tech.metavm.entity.IndexQueryOperator;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class IndexQueryPO {
    private long tenantId;
    private long constraintId;
    private IndexKeyPO key;
    private IndexQueryOperator columnXOperator;
    private boolean desc;
    private long limit;

    public IndexQueryPO(long tenantId,
                        long constraintId,
                        IndexKeyPO key,
                        IndexQueryOperator columnXOperator,
                        boolean desc,
                        long limit) {
        this.tenantId = tenantId;
        this.constraintId = constraintId;
        this.key = key;
        this.columnXOperator = columnXOperator;
        this.desc = desc;
        this.limit = limit;
    }

    public IndexQueryPO() {
    }

    public List<IndexEntryPO> execute(Collection<IndexEntryPO> indexItems) {
        return NncUtils.filterAndSortAndLimit(
                indexItems,
                this::matches,
                getItemComparator(),
                limit
        );
    }


    private Comparator<IndexEntryPO> getItemComparator() {
        return desc ? (i1,i2) -> Long.compare(i2.getColumnX(), i1.getColumnX())
                : Comparator.comparingLong(IndexEntryPO::getColumnX);
    }

    private boolean matches(IndexEntryPO indexItem) {
        if(tenantId != indexItem.getTenantId() && constraintId != indexItem.getConstraintId()) {
            return false;
        }
        for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
            if(!indexItem.getColumn(i).equals(key.getColumn(i))) {
                return false;
            }
        }
        return columnXOperator.evaluate(indexItem.getColumnX(), this.key.getColumnX());
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getConstraintId() {
        return constraintId;
    }

    public void setConstraintId(long constraintId) {
        this.constraintId = constraintId;
    }

//    public List<IndexQueryItemPO> getItems() {
//        return items;
//    }

//    public void setItems(List<IndexQueryItemPO> items) {
//        this.items = items;
//    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public IndexKeyPO getKey() {
        return key;
    }

    public void setKey(IndexKeyPO key) {
        this.key = key;
    }

    public IndexQueryOperator getColumnXOperator() {
        return columnXOperator;
    }

    public void setColumnXOperator(IndexQueryOperator columnXOperator) {
        this.columnXOperator = columnXOperator;
    }
}
