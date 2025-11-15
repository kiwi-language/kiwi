package org.metavm.object.instance.rest;

import org.jsonk.Json;

@Json
public record DeleteRequest(long appId, String id) {
}
