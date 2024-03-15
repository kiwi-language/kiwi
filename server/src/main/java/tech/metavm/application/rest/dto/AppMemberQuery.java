package tech.metavm.application.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record AppMemberQuery(
        String appId,
        @Nullable String searchText,
        int page,
        int pageSize,
        @Nullable List<String> excluded
) {
}
