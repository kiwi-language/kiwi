package org.metavm.manufacturing.production.dto;

import org.metavm.api.Value;
import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.production.FeedType;
import org.metavm.manufacturing.production.Routing;
import org.metavm.manufacturing.production.WorkCenter;

import javax.annotation.Nullable;
import java.util.List;

@Value
public record RoutingDTO(
        @Nullable Routing entity,
        String name, Material product, Unit unit, List<ProcessDTO> processes, List<SuccessionDTO> successions
) {

    @Value
    public record ProcessDTO(
            @Nullable Routing.Process entity,
            String processCode,
            int sequence,
            org.metavm.manufacturing.production.Process process,
            @Nullable WorkCenter workCenter,
            @Nullable String processDescription,
            List<ItemDTO> items
    ) {

        @Value
        public record ItemDTO(@Nullable Routing.Process.Item entity, int sequence, long numerator) {}

    }

    @Value
    public record SuccessionDTO(Routing.Succession entity,
                                int fromSeq,
                                int toSeq,
                                Material product,
                                Unit unit,
                                double baseQuantity,
                                boolean report,
                                boolean inbound,
                                boolean autoInbound,
                                QualityInspectionState qualityInspectionState,
                                FeedType feedType) {

    }


}
