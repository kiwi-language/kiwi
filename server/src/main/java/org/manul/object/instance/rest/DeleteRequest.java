package org.manul.object.instance.rest;

import org.jsonk.Json;

@Json
public record DeleteRequest(long appId, String id) {
}
