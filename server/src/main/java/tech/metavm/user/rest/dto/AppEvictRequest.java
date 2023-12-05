package tech.metavm.user.rest.dto;

import java.util.List;

public record AppEvictRequest(
        Long appId,
        List<Long> userIds
) {
}
