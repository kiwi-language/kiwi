package org.manul.entity;

import org.manul.api.JsonIgnore;
import org.manul.object.type.ResolutionStage;

public interface StagedEntity {

    @JsonIgnore
    ResolutionStage getStage();

}
