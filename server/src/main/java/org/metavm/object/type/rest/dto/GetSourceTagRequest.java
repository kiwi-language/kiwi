package org.metavm.object.type.rest.dto;

import org.jsonk.Json;

@Json
public record GetSourceTagRequest(long appId, String name) {
}
