package tech.metavm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.util.Constants;

public record RefDTO(Long id, Long tmpId) {

    public RefDTO {
        if(id != null) tmpId = null;
    }

    @JsonIgnore
    public String getIdString() {
        return id != null ? Constants.CONSTANT_ID_PREFIX + id : Constants.CONSTANT_TMP_ID_PREFIX + tmpId;
    }

    @JsonIgnore
    public boolean isPersisted() {
        return id != null;
    }

    public static RefDTO ofId(Long id) {
        return new RefDTO(id, null);
    }

    public static RefDTO ofTmpId(Long tmpId) {
        return new RefDTO(null, tmpId);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return id == null && tmpId == null;
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
