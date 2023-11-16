package tech.metavm.object.type.persistence.query;

import tech.metavm.object.type.TypeCategory;

import java.util.List;
import java.util.Objects;

public final class TypeQuery {
    private final long tenantId;
    private final List<TypeCategory> categories;
    private final String searchText;
    private final Boolean anonymous;
    private final int page;
    private final int pageSize;

    public TypeQuery(long tenantId, List<TypeCategory> categories, String searchText, Boolean anonymous, int page, int pageSize) {
        this.tenantId = tenantId;
        this.categories = categories;
        this.searchText = searchText;
        this.anonymous = anonymous;
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

    public List<TypeCategory> getCategories() {
        return categories;
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

    public Boolean getAnonymous() {
        return anonymous;
    }
}
