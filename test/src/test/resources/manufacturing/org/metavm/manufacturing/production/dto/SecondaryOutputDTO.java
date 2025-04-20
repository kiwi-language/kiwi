package org.metavm.manufacturing.production.dto;

import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.production.Routing;

public record SecondaryOutputDTO(
        int seq,
        Material product,
        double baseFigure,
        Unit unit,
        boolean inbound,
        boolean autoInbound,
        Routing.Process process
) {
}
