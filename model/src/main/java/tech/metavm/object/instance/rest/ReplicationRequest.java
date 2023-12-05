package tech.metavm.object.instance.rest;

import java.util.List;

public record ReplicationRequest(List<Long> typeIds) {
}
