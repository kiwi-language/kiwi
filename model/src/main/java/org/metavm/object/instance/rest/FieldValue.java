package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;
import java.util.Set;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = PrimitiveFieldValue.class, name = "1"),
                @JsonSubTypes.Type(value = ReferenceFieldValue.class, name = "2"),
                @JsonSubTypes.Type(value = ArrayFieldValue.class, name = "3"),
                @JsonSubTypes.Type(value = InstanceFieldValue.class, name = "4"),
                @JsonSubTypes.Type(value = ExpressionFieldValue.class, name = "5"),
                @JsonSubTypes.Type(value = ListFieldValue.class, name = "7"),
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
public abstract class FieldValue {
    private final String displayValue;

    public FieldValue(@JsonProperty("displayValue") String displayValue) {
        this.displayValue = displayValue;
    }

    public abstract int getKind();

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

    public abstract boolean valueEquals(FieldValue that, Set<String> newIds);

    public InstanceDTO underlyingInstance() {
        throw new UnsupportedOperationException();
    }

    public String referenceId() {
        throw new UnsupportedOperationException();
    }

    public abstract Object toJson();

}
