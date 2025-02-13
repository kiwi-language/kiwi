package org.metavm.manufacturing.production.dto;

import org.metavm.manufacturing.material.Material;
import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.material.Unit;
import org.metavm.manufacturing.production.*;

import javax.annotation.Nullable;
import java.util.List;

public record ComponentMaterialDTO(
        int sequence,
        Material material,
        Unit unit,
        long numerator,
        long denominator,
        double attritionRate,
        @Nullable BOM version,
        PickMethod pickMethod,
        boolean routingSpecified,
        @Nullable Routing.Process process,
        QualityInspectionState qualityInspectionState,
        FeedType feedType,
        List<ComponentMaterialItemDTO> items
) {
}
