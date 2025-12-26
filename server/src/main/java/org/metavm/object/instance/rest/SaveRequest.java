package org.metavm.object.instance.rest;

import org.jsonk.Json;

import java.util.Map;

@Json
public record SaveRequest(long appId, Map<String, Object> object) {
}
