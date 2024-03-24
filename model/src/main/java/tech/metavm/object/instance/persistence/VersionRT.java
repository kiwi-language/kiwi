package tech.metavm.object.instance.persistence;

import tech.metavm.object.instance.core.Id;

public record VersionRT(long appId, Id id, long version) {
}
