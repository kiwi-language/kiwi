package tech.metavm.message.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record MessageQuery(
        @Nullable String searchText,
        Boolean read,
        int page,
        int pageSize,
        @Nullable List<Long> newlyCreated
        ) {
}
