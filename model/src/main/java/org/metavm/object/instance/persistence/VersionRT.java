package org.metavm.object.instance.persistence;

import org.metavm.object.instance.core.Id;

public record VersionRT(long appId, Id id, long version, int entityTag) {
}
