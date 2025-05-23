package org.metavm.object.instance.rest.dto;

import org.metavm.api.dto.ClassTypeDTO;

import java.util.List;

public record SearchRequest(
        ClassTypeDTO type,
        List<SearchTerm> terms,
        int page,
        int pageSize
) {

    @Override
    public int page() {
        return page > 0 ? page : 1;
    }

    @Override
    public int pageSize() {
        return pageSize > 0 ? pageSize : 20;
    }
}
