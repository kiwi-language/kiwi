package tech.metavm.application.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record AppMemberQuery(
        long appId,
        @Nullable String searchText,
        int page,
        int pageSize,
        @Nullable List<Long> excluded
) {
}
