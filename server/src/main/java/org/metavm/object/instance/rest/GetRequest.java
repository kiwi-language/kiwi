package org.metavm.object.instance.rest;

import org.jsonk.Json;

@Json
public record GetRequest(long appId, String id) {
}
