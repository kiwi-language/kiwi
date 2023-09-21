package tech.metavm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RefDTO(Long id, Long tmpId) {

    public RefDTO {
        if(id != null) tmpId = null;
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
