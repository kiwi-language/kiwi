package org.manul.user.rest.dto;

import java.util.List;

public record AppEvictRequest(
        long appId,
        List<String> userIds
) {
}
