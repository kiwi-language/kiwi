package org.metavm.manufacturing.material.dto;

import org.metavm.manufacturing.material.MaterialKind;
import org.metavm.manufacturing.material.TimeUnit;
import org.metavm.manufacturing.material.Unit;

import javax.annotation.Nullable;

public record MaterialDTO(
        @Nullable Object id,
        String code,
        String name,
        MaterialKind kind,
        Unit unit,
        int storageValidPeriod,
        TimeUnit storageValidPeriodUnit
) {
}
