package org.metavm.flow.rest;

import java.util.List;

public record BranchNodeParam(
        boolean inclusive,
        List<BranchDTO> branches
//        List<BranchNodeOutputFieldDTO> fields
) {
}
