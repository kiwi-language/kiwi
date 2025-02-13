package org.metavm.manufacturing.production.dto;

import org.metavm.manufacturing.GeneralState;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.production.Routing;

import javax.annotation.Nullable;
import java.util.List;

public record BomDTO(
        Material product,
        Unit unit,
        @Nullable Routing routing,
        Routing.Process reportingProcess,
        GeneralState state,
        boolean inbound,
        boolean autoInbound,
        List<ComponentMaterialDTO> components,
        List<SecondaryOutputDTO> secondaryOutputs
) {
}
