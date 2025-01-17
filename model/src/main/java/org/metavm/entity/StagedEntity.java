package org.metavm.entity;

import org.metavm.api.JsonIgnore;
import org.metavm.object.type.ResolutionStage;

public interface StagedEntity {

    @JsonIgnore
    ResolutionStage getStage();

}
