package tech.metavm.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.util.CommonConstants;

import java.util.Objects;

public record RefDTO(Long id, Long tmpId) {

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

    public static RefDTO fromId(Long id) {
        return new RefDTO(id, null);
    }

    public static RefDTO fromTmpId(Long tmpId) {
        return new RefDTO(null, tmpId);
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return tmpId != null ? "tmpId-" + tmpId : "id-" + id;
    }

    public Id toId() {
        if(id != null)
            return new PhysicalId(id);
        else
            return new TmpId(Objects.requireNonNull(tmpId));
    }

}
