package org.metavm.object.instance.rest.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ObjectDTO.class, name = "object"),
                @JsonSubTypes.Type(value = ReferencedTO.class, name = "reference"),
                @JsonSubTypes.Type(value = EnumConstantDTO.class, name = "enumConstant"),
                @JsonSubTypes.Type(value = BeanDTO.class, name = "bean"),
                @JsonSubTypes.Type(value = StringValueDTO.class, name = "string"),
                @JsonSubTypes.Type(value = IntValueDTO.class, name = "int"),
                @JsonSubTypes.Type(value = FloatValueDTO.class, name = "float"),
                @JsonSubTypes.Type(value = BoolValueDTO.class, name = "bool"),
                @JsonSubTypes.Type(value = BoolValueDTO.class, name = "null"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
sealed public interface ValueDTO
        permits ArrayDTO, BeanDTO, BoolValueDTO, EnumConstantDTO, FloatValueDTO, IntValueDTO, NullDTO, ObjectDTO, ReferencedTO, StringValueDTO {

    String getKind();

}
