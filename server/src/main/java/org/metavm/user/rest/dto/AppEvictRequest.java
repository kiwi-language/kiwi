package org.metavm.user.rest.dto;

import java.util.List;

public record AppEvictRequest(
        long appId,
        List<String> userIds
) {
}
