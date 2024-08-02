package org.metavm.object.instance.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.object.view.ObjectMapping;
import org.metavm.object.view.rest.dto.MappingKey;
import org.metavm.object.view.rest.dto.ObjectMappingRefDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record SourceRef(
        Reference source,
        @Nullable ObjectMapping mapping
) {

    @JsonIgnore
    public @Nullable MappingKey getMappingKey() {
        return NncUtils.get(mapping, ObjectMapping::toKey);
    }

    @JsonIgnore
    public @Nullable ObjectMappingRefDTO getMappingRefDTO() {
        return NncUtils.get(mapping, m -> m.getRef().toDTO());
    }

}
