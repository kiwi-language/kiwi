package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityType;

@EntityType
public enum QualityInspectionState {
    QUALIFIED,
    WAITING,
    UNQUALIFIED,
    CONCESSION_ACCEPTED

}
