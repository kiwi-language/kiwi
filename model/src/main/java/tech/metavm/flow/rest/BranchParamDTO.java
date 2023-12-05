package tech.metavm.flow.rest;

import java.util.List;

public record BranchParamDTO(
        boolean inclusive,
        List<BranchDTO> branches
//        List<BranchNodeOutputFieldDTO> fields
) {
}
