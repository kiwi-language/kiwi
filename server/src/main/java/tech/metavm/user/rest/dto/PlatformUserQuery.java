package tech.metavm.user.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record PlatformUserQuery(
        @Nullable Long appId,
        @Nullable String searchText,
        @Nullable String loginName,
        @Nullable List<Long> excluded,
        int page,
        int pageSize
) {
}
