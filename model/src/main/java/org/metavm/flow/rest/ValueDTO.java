package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.object.instance.rest.FieldValue;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = ConstantValueDTO.class),
                @JsonSubTypes.Type(name = "2", value = NeverValueDTO.class),
                @JsonSubTypes.Type(name = "3", value = TypeValueDTO.class),
                @JsonSubTypes.Type(name = "4", value = NodeValueDTO.class),
                @JsonSubTypes.Type(name = "5", value = ArrayValueDTO.class),
                @JsonSubTypes.Type(name = "6", value = PropertyValueDTO.class),
                @JsonSubTypes.Type(name = "7", value = ExpressionValueDTO.class),
        }
)
public interface ValueDTO {

    static ValueDTO constValue(FieldValue value) {
        return new ConstantValueDTO(value);
    }

    static ValueDTO exprValue(String expr) {
        return new ExpressionValueDTO(expr);
    }

    int getKind();

}
