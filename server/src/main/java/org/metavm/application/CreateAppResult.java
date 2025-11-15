package org.metavm.application;

import org.jsonk.Json;

@Json
public record CreateAppResult(long appId, String ownerId) {
}
