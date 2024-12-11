package org.metavm.manufacturing.material;

import org.metavm.api.Entity;

@Entity
public enum QualityInspectionState {
    QUALIFIED,
    WAITING,
    UNQUALIFIED,
    CONCESSION_ACCEPTED

}
