package tech.metavm.flow.rest;

import java.util.Objects;

public final class FlowQuery {
    private final long tenantId;
    private final long typeId;
    private final int page;
    private final int pageSize;
    private final String name;

    public FlowQuery(
            long tenantId,
            long typeId,
            int page,
            int pageSize,
            String name
    ) {
        this.tenantId = tenantId;
        this.typeId = typeId;
        this.page = page;
        this.pageSize = pageSize;
        this.name = name;
    }

    public long getStart() {
        return (long) (page - 1) * pageSize;
    }

    public long getLimit() {
        return pageSize;
    }

    public long getTenantId() {
        return tenantId;
    }

    public long getTypeId() {
        return typeId;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FlowQuery) obj;
        return this.tenantId == that.tenantId &&
                this.typeId == that.typeId &&
                this.page == that.page &&
                this.pageSize == that.pageSize &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, typeId, page, pageSize, name);
    }

    @Override
    public String toString() {
        return "FlowQuery[" +
                "tenantId=" + tenantId + ", " +
                "typeId=" + typeId + ", " +
                "page=" + page + ", " +
                "pageSize=" + pageSize + ", " +
                "name=" + name + ']';
    }


}
