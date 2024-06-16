package org.metavm.manufacturing.material;

import org.metavm.api.EntityType;

@EntityType
public enum QualityInspectionState {
    QUALIFIED,
    WAITING,
    UNQUALIFIED,
    CONCESSION_ACCEPTED

}
