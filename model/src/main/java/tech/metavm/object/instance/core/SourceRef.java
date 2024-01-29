package tech.metavm.object.instance.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.Entity;
import tech.metavm.object.view.Mapping;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record SourceRef(
        DurableInstance source,
        @Nullable Mapping mapping
) {

    @JsonIgnore
    public @Nullable Long getMappingId() {
        return NncUtils.get(mapping, Entity::getId);
    }

}
