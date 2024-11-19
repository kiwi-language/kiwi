package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = MethodRefKey.class, name = "1"),
                @JsonSubTypes.Type(value = FunctionRefKey.class, name = "2")
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface CallableRefKey {

    int getKind();

}
