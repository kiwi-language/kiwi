package org.metavm.manufacturing.production.dto;

import org.metavm.manufacturing.material.QualityInspectionState;
import org.metavm.manufacturing.production.FeedType;
import org.metavm.manufacturing.production.Routing;

public record ComponentMaterialItemDTO(
        int sequence,
        int numerator,
        int denominator,
        Routing.Process process,
        QualityInspectionState qualityInspectionState,
        FeedType feedType
) {
}
