package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import java.util.List;

public record ScopeDTO(
        String id,
        List<NodeDTO> nodes
) implements BaseDTO, Copyable<ScopeDTO> {

    @Override
    public ScopeDTO copy(CopyContext context) {
        return new ScopeDTO(context.mapId(id), NncUtils.map(nodes, context::copy));
    }
}
