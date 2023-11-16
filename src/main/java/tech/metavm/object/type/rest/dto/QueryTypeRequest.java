package tech.metavm.object.type.rest.dto;

import tech.metavm.common.Request;

import java.util.List;

public final class QueryTypeRequest extends Request {
    private final String searchText;
    private final List<Integer> categories;
    private final Boolean isTemplate;
    private final boolean includeAnonymous;
    private final boolean includeBuiltin;
    private final Boolean error;
    private final int page;
    private final int pageSize;

    public QueryTypeRequest(
            String searchText,
            List<Integer> categories,
            Boolean isTemplate,
            boolean includeAnonymous,
            boolean includeBuiltin,
            Boolean error,
            int page,
            int pageSize
    ) {
        this.searchText = searchText;
        this.categories = categories;
        this.isTemplate = isTemplate;
        this.includeAnonymous = includeAnonymous;
        this.includeBuiltin = includeBuiltin;
        this.error = error;
        this.page = page;
        this.pageSize = pageSize;
    }

    public String getSearchText() {
        return searchText;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public Boolean isTemplate() {
        return isTemplate;
    }

    public boolean isIncludeAnonymous() {
        return includeAnonymous;
    }

    public boolean isIncludeBuiltin() {
        return includeBuiltin;
    }

    public Boolean getError() {
        return error;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

}
