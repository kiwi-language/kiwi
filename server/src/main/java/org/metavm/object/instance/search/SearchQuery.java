package org.metavm.object.instance.search;


import javax.annotation.Nullable;
import java.util.Set;

public record SearchQuery(
        long appId,
        Set<String> types,
        @Nullable SearchCondition condition,
        boolean includeBuiltin,
        int page,
        int pageSize,
        int extra
) {

    public SearchQuery {
        if(types.isEmpty())
            throw new IllegalArgumentException("types is empty");
    }

    public int from() {
        return (page - 1) * pageSize;
    }

    public int size() {
        return pageSize + extra;
    }

    public int end() {
        return from() + size();
    }

}
