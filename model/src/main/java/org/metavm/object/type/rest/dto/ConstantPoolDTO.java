package org.metavm.object.type.rest.dto;

import java.util.List;

public record ConstantPoolDTO(List<CpEntryDTO> entries) {
}
