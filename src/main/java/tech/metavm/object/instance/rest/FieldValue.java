package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = PrimitiveFieldValue.class, name = "1"),
                @JsonSubTypes.Type(value = ReferenceFieldValueDTO.class, name = "2"),
                @JsonSubTypes.Type(value = ArrayFieldValueDTO.class, name = "3"),
                @JsonSubTypes.Type(value = InstanceFieldValueDTO.class, name = "4"),
                @JsonSubTypes.Type(value = ExpressionFieldValueDTO.class, name = "5"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public abstract class FieldValue {
    private final int type;
    private final String displayValue;

    public FieldValue(@JsonProperty("type") int type,
                      @JsonProperty("displayValue") String displayValue) {
        this.type = type;
        this.displayValue = displayValue;
    }

    public int getType() {
        return type;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldValue that = (FieldValue) o;
        return Objects.equals(displayValue, that.displayValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayValue);
    }
}
