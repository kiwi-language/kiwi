package org.metavm.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.util.CommonConstants;

public record RefDTO(Long id, Long tmpId, long typeId) {

    public RefDTO {
        if (id != null) tmpId = null;
    }

    @JsonIgnore
    public String getIdString() {
        return id != null ? CommonConstants.CONSTANT_ID_PREFIX + id : CommonConstants.CONSTANT_TMP_ID_PREFIX + tmpId;
    }

    @JsonIgnore
    public boolean isPersisted() {
        return id != null && id != 0L;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (id == null || id == 0L) && tmpId == null;
    }

    public static RefDTO fromId(Long id, long typeId) {
        return new RefDTO(id, null, typeId);
    }

    public static RefDTO fromTmpId(Long tmpId, long typeId) {
        return new RefDTO(null, tmpId, typeId);
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return tmpId != null ? "tmpId-" + tmpId : "id-" + id;
    }

}
