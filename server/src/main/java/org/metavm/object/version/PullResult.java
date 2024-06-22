package org.metavm.object.version;

import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.List;

public record PullResult(long version, List<KlassDTO> types, List<Long> removedTypeIds) {
}
