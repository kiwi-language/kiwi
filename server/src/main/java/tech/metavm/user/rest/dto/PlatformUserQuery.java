package tech.metavm.user.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record PlatformUserQuery(
        @Nullable String appId,
        @Nullable String searchText,
        @Nullable String loginName,
        @Nullable List<String> excluded,
        int page,
        int pageSize
) {
}
