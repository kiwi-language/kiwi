package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ValueCpEntryDTO.class, name = "0"),
                @JsonSubTypes.Type(value = TypeCpEntryDTO.class, name = "1"),
                @JsonSubTypes.Type(value = FieldCpEntryDTO.class, name = "2"),
                @JsonSubTypes.Type(value = MethodCpEntryDTO.class, name = "3"),
                @JsonSubTypes.Type(value = FunctionCpEntryDTO.class, name = "4"),
                @JsonSubTypes.Type(value = MappingCpEntryDTO.class, name = "5"),
                @JsonSubTypes.Type(value = IndexCpEntryDTO.class, name = "6"),
                @JsonSubTypes.Type(value = LambdaCpEntryDTO.class, name = "7"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface CpEntryDTO {

    int index();

    int getKind();

}
