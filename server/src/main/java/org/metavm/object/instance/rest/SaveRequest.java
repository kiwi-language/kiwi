package org.metavm.object.instance.rest;

import java.util.Map;

public record SaveRequest(long appId, Map<String, Object> object) {
}
