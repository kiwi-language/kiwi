package tech.metavm.object.meta.persistence.query;

import java.util.Objects;

public final class TypeQuery {
    private final long tenantId;
    private final Integer type;
    private final String searchText;
    private final int page;
    private final int pageSize;

    public TypeQuery(long tenantId, Integer type, String searchText, int page, int pageSize) {
        this.tenantId = tenantId;
        this.type = type;
        this.searchText = searchText;
        this.page = page;
        this.pageSize = pageSize;
    }

    public long getStart() {
        return (long) (page - 1) * pageSize;
    }

    public long getLimit() {
        return pageSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TypeQuery) obj;
        return this.tenantId == that.tenantId &&
                this.page == that.page &&
                this.pageSize == that.pageSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, page, pageSize);
    }

    @Override
    public String toString() {
        return "NClassQuery[" +
                "tenantId=" + tenantId + ", " +
                "page=" + page + ", " +
                "pageSize=" + pageSize + ']';
    }

    public long tenantId() {
        return tenantId;
    }

    public Integer type() {
        return type;
    }

    public int page() {
        return page;
    }

    public int pageSize() {
        return pageSize;
    }

    public String getSearchText() {
        return searchText;
    }
}
