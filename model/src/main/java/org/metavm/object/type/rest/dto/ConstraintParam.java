package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.object.type.IndexParam;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = IndexParam.class),
                @JsonSubTypes.Type(name = "2", value = CheckConstraintParam.class),
        }
)
public interface ConstraintParam {
}
