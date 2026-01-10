package org.manul.object.instance.persistence;

import org.manul.object.instance.core.Id;

public record VersionRT(long appId, Id id, long version, int entityTag) {
}
