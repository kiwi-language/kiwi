package tech.metavm.system.rest.dto;

import java.util.List;

public record GetActiveBlocksRequest(List<Long> typeIds) {
}
