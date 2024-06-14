package org.metavm.object.version;

import org.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public record PullResult(long version, List<TypeDTO> types, List<Long> removedTypeIds) {
}
