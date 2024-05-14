package tech.metavm.object.instance.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.object.view.rest.dto.MappingKey;
import tech.metavm.object.view.rest.dto.ObjectMappingRefDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record SourceRef(
        DurableInstance source,
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
