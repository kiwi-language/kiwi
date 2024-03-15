package tech.metavm.user.rest.dto;

import java.util.List;

public record AppEvictRequest(
        String appId,
        List<String> userIds
) {
}
