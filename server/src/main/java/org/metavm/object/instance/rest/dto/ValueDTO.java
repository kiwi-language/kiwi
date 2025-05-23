package org.metavm.object.instance.rest.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.object.instance.core.ShortValue;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ObjectDTO.class, name = "object"),
                @JsonSubTypes.Type(value = ReferencedTO.class, name = "reference"),
                @JsonSubTypes.Type(value = EnumConstantDTO.class, name = "enumConstant"),
                @JsonSubTypes.Type(value = BeanDTO.class, name = "bean"),
                @JsonSubTypes.Type(value = StringValueDTO.class, name = "string"),
                @JsonSubTypes.Type(value = ByteValueDTO.class, name = "byte"),
                @JsonSubTypes.Type(value = ShortValue.class, name = "short"),
                @JsonSubTypes.Type(value = IntValueDTO.class, name = "int"),
                @JsonSubTypes.Type(value = LongValueDTO.class, name = "long"),
                @JsonSubTypes.Type(value = FloatValueDTO.class, name = "float"),
                @JsonSubTypes.Type(value = DoubleValueDTO.class, name = "double"),
                @JsonSubTypes.Type(value = BoolValueDTO.class, name = "bool"),
                @JsonSubTypes.Type(value = CharValueDTO.class, name = "char"),
                @JsonSubTypes.Type(value = BoolValueDTO.class, name = "null"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
sealed public interface ValueDTO
        permits ArrayDTO, BeanDTO, BoolValueDTO, CharValueDTO, EnumConstantDTO, NullDTO, NumberValueDTO, ObjectDTO, ReferencedTO, StringValueDTO {

    String getKind();

}
